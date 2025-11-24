package com.linyi.cropseed.trace.module.trace.service.impl;

import com.alibaba.fastjson.JSON;
import com.linyi.cropseed.trace.config.TBaasConfig.TBaasProperties;
import com.linyi.cropseed.trace.module.trace.model.vo.BlockchainProof;
import com.linyi.cropseed.trace.module.trace.model.vo.TraceChainData;
import com.linyi.cropseed.trace.module.trace.model.entity.TraceRecord;
import com.linyi.cropseed.trace.module.trace.service.BlockchainService;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.tbaas.v20180416.TbaasClient;
import com.tencentcloudapi.tbaas.v20180416.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * TBaas区块链服务实现
 *
 * @author linyi
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TBaasBlockchainServiceImpl implements BlockchainService {

    private final TbaasClient tbaasClient;
    private final TBaasProperties tbaasProperties;

    @Override
    public String uploadTraceRecord(TraceRecord traceRecord) {
        int maxAttempts = tbaasProperties.getRetry().getMaxAttempts();
        long retryDelay = tbaasProperties.getRetry().getDelay();

        Exception lastException = null;

        // 重试机制
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("尝试上链: traceCode={}, 第{}/{}次", traceRecord.getTraceCode(), attempt, maxAttempts);

                // 构建上链数据
                TraceChainData chainData = TraceChainData.builder()
                    .traceCode(traceRecord.getTraceCode())
                    .batchId(traceRecord.getBatchId())
                    .recordType(traceRecord.getRecordType())
                    .recordStage(traceRecord.getRecordStage())
                    .operatorName(traceRecord.getOperatorName())
                    .recordTime(traceRecord.getRecordTime())
                    .location(traceRecord.getLocation())
                    .contentSummary(traceRecord.getContentSummary())
                    .entityName(traceRecord.getEntityName())
                    .qualityGrade(traceRecord.getQualityGrade())
                    .testResult(traceRecord.getTestResult())
                    .dataHash(calculateDataHash(traceRecord))
                    .timestamp(System.currentTimeMillis())
                    .build();

                // 创建请求
                InvokeChainMakerDemoContractRequest req = new InvokeChainMakerDemoContractRequest();
                req.setClusterId(tbaasProperties.getClusterId());
                req.setChainId(tbaasProperties.getChainId());
                req.setContractName(tbaasProperties.getContractName());
                req.setFuncName("save");

                //  设置参数 - 使用Map格式符合TBaas要求
                Map<String, String> params = new HashMap<>();
                params.put("key", traceRecord.getTraceCode());
                params.put("field", "traceData");
                params.put("value", JSON.toJSONString(chainData));
                req.setFuncParam(JSON.toJSONString(params));
                req.setAsyncFlag(0L); // 同步执行

                //  调用合约
                InvokeChainMakerDemoContractResponse resp = tbaasClient.InvokeChainMakerDemoContract(req);

                // 检查结果
                if (resp.getResult() != null && resp.getResult().getCode() == 0) {
                    String txHash = resp.getResult().getTxId();
                    log.info("溯源记录上链成功: traceCode={}, txHash={}, 尝试次数={}",
                        traceRecord.getTraceCode(), txHash, attempt);

                    // 异步验证上链结果
                    CompletableFuture.runAsync(() -> verifyChainTransaction(txHash));

                    return txHash;
                } else {
                    String errorMsg = resp.getResult() != null ? resp.getResult().getCodeMessage() : "未知错误";
                    throw new RuntimeException("合约执行失败: " + errorMsg);
                }

            } catch (TencentCloudSDKException e) {
                lastException = e;
                log.warn("TBaas调用失败(第{}/{}次): code={}, message={}",
                    attempt, maxAttempts, e.getErrorCode(), e.getMessage());

                // 如果不是最后一次尝试，等待后重试
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("上链存储失败(第{}/{}次): {}", attempt, maxAttempts, e.getMessage());

                // 如果不是最后一次尝试，等待后重试
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }

        // 所有重试都失败
        log.error("溯源记录上链失败，已重试{}次: traceCode={}", maxAttempts, traceRecord.getTraceCode());
        if (lastException instanceof TencentCloudSDKException) {
            TencentCloudSDKException sdkEx = (TencentCloudSDKException) lastException;
            throw new RuntimeException("区块链调用异常(重试" + maxAttempts + "次后失败): " + sdkEx.getMessage(), sdkEx);
        } else {
            throw new RuntimeException("上链存储失败(重试" + maxAttempts + "次后失败)", lastException);
        }
    }

    @Override
    public TraceChainData queryChainTraceRecord(String traceCode) {
        try {
            // 创建查询请求
            QueryChainMakerDemoContractRequest req = new QueryChainMakerDemoContractRequest();
            req.setClusterId(tbaasProperties.getClusterId());
            req.setChainId(tbaasProperties.getChainId());
            req.setContractName(tbaasProperties.getContractName());
            req.setFuncName("get");

            // 设置查询参数 - 使用Map格式符合TBaas要求
            Map<String, String> params = new HashMap<>();
            params.put("key", traceCode);
            params.put("field", "traceData");
            req.setFuncParam(JSON.toJSONString(params));

            // 执行查询
            QueryChainMakerDemoContractResponse resp = tbaasClient.QueryChainMakerDemoContract(req);

            //  解析结果
            if (resp.getResult() != null && resp.getResult().getCode() == 0) {
                String result = resp.getResult().getResult();
                if (result != null && !result.isEmpty() && !result.equals("null")) {
                    try {
                        // 链上数据是Base64编码的JSON，需要先解码
                        String decodedJson = new String(java.util.Base64.getDecoder().decode(result), java.nio.charset.StandardCharsets.UTF_8);
                        log.debug("解码后的链上数据: traceCode={}, json={}", traceCode, decodedJson);

                        // 解析JSON为TraceChainData对象
                        TraceChainData chainData = JSON.parseObject(decodedJson, TraceChainData.class);
                        log.info("查询链上溯源记录成功: traceCode={}", traceCode);
                        return chainData;
                    } catch (Exception e) {
                        log.warn("解析链上数据失败: traceCode={}, result={}, error={}", traceCode, result, e.getMessage());
                        return null;
                    }
                } else {
                    log.info("链上未找到溯源记录: traceCode={}", traceCode);
                }
            } else {
                String errorMsg = resp.getResult() != null ? resp.getResult().getCodeMessage() : "未知错误";
                log.warn("链上查询失败: traceCode={}, error={}", traceCode, errorMsg);
            }

            return null;

        } catch (TencentCloudSDKException e) {
            log.error("TBaas查询异常: code={}, message={}", e.getErrorCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("查询链上溯源记录异常", e);
            return null;
        }
    }

    @Override
    public boolean verifyTraceIntegrity(String traceCode, TraceRecord localRecord) {
        try {
            TraceChainData chainData = queryChainTraceRecord(traceCode);
            if (chainData == null) {
                log.warn("链上未找到溯源记录: traceCode={}", traceCode);
                return false;
            }

            // 计算本地数据哈希
            String localHash = calculateDataHash(localRecord);

            // 对比链上数据哈希
            boolean verified = localHash.equals(chainData.getDataHash());
            log.info("溯源数据完整性验证: traceCode={}, verified={}", traceCode, verified);

            return verified;

        } catch (Exception e) {
            log.error("溯源数据完整性验证失败: traceCode={}", traceCode, e);
            return false;
        }
    }

    @Override
    public BlockchainProof generateTraceProof(String traceCode) {
        try {
            TraceChainData chainData = queryChainTraceRecord(traceCode);
            if (chainData == null) {
                throw new RuntimeException("该溯源码尚未上链到区块链，无法生成证明。请先确保记录已成功上链。");
            }

            // 查询交易详情
            if (chainData.getTxId() != null) {
                ChainMakerTransactionResult txResult = queryTransaction(chainData.getTxId());

                return BlockchainProof.builder()
                    .traceCode(traceCode)
                    .txHash(chainData.getTxId())
                    .blockHeight(txResult.getBlockHeight())
                    .timestamp(chainData.getTimestamp())
                    .dataHash(chainData.getDataHash())
                    .txStatus(txResult.getCode().intValue())
                    .verified(true)
                    .build();
            } else {
                return BlockchainProof.builder()
                    .traceCode(traceCode)
                    .dataHash(chainData.getDataHash())
                    .timestamp(chainData.getTimestamp())
                    .verified(true)
                    .build();
            }

        } catch (Exception e) {
            log.error("生成区块链证明失败: traceCode={}", traceCode, e);
            throw new RuntimeException("生成区块链证明失败", e);
        }
    }

    @Override
    public boolean isTransactionConfirmed(String txHash) {
        try {
            ChainMakerTransactionResult result = queryTransaction(txHash);
            return result != null && result.getCode() == 0;
        } catch (Exception e) {
            log.error("查询交易状态失败: txHash={}", txHash, e);
            return false;
        }
    }

    /**
     * 查询交易详情
     */
    private ChainMakerTransactionResult queryTransaction(String txHash) {
        try {
            QueryChainMakerDemoTransactionRequest req = new QueryChainMakerDemoTransactionRequest();
            req.setClusterId(tbaasProperties.getClusterId());
            req.setChainId(tbaasProperties.getChainId());
            req.setTxID(txHash);

            QueryChainMakerDemoTransactionResponse resp = tbaasClient.QueryChainMakerDemoTransaction(req);
            return resp.getResult();

        } catch (TencentCloudSDKException e) {
            log.error("查询交易详情异常: txHash={}, code={}, message={}",
                txHash, e.getErrorCode(), e.getMessage());
            throw new RuntimeException("交易查询异常", e);
        }
    }

    /**
     * 计算数据哈希
     */
    private String calculateDataHash(TraceRecord record) {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getTraceCode())
          .append(record.getBatchId())
          .append(record.getRecordType())
          .append(record.getContentSummary())
          .append(record.getOperatorName());

        if (record.getRecordTime() != null) {
            sb.append(record.getRecordTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        return DigestUtils.md5DigestAsHex(sb.toString().getBytes());
    }

    /**
     * 异步验证交易状态
     */
    private void verifyChainTransaction(String txHash) {
        try {
            Thread.sleep(3000); // 等待上链确认
            ChainMakerTransactionResult result = queryTransaction(txHash);
            if (result != null && result.getCode() == 0) {
                log.info("交易上链成功: txHash={}, blockHeight={}", txHash, result.getBlockHeight());
            } else {
                String errorMsg = result != null ? result.getCodeMessage() : "未知错误";
                log.error("交易上链失败: txHash={}, error={}", txHash, errorMsg);
            }
        } catch (Exception e) {
            log.error("验证交易状态异常: txHash={}", txHash, e);
        }
    }
}
