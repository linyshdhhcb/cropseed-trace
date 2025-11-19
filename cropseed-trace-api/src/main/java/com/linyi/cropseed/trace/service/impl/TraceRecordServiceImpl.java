package com.linyi.cropseed.trace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.dto.blockchain.BlockchainProof;
import com.linyi.cropseed.trace.entity.TraceRecord;
import com.linyi.cropseed.trace.mapper.TraceRecordMapper;
import com.linyi.cropseed.trace.service.TraceRecordService;
import com.linyi.cropseed.trace.service.blockchain.BlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 溯源记录服务实现类
 *
 * @author linyi
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TraceRecordServiceImpl extends ServiceImpl<TraceRecordMapper, TraceRecord> implements TraceRecordService {

    private final TraceRecordMapper traceRecordMapper;
    private final BlockchainService blockchainService;
    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTraceRecord(TraceRecord traceRecord) {
        RLock lock = redissonClient.getLock("trace:create:" + traceRecord.getTraceCode());
        try {
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                // 1. 先保存到数据库（确保业务数据安全）
                traceRecord.setBlockchainStatus(0); // 未上链
                traceRecord.setCreateTime(LocalDateTime.now());
                traceRecord.setUpdateTime(LocalDateTime.now());

                boolean saved = this.save(traceRecord);
                if (!saved) {
                    throw new RuntimeException("保存溯源记录失败");
                }

                Long recordId = traceRecord.getId();
                log.info("溯源记录保存成功: recordId={}, traceCode={}", recordId, traceRecord.getTraceCode());

                // 2. 异步上链
                CompletableFuture.supplyAsync(() -> {
                    try {
                        // 更新状态为上链中
                        updateBlockchainStatus(recordId, null, 1);

                        // 执行上链
                        String txHash = blockchainService.uploadTraceRecord(traceRecord);

                        // 更新为上链成功
                        updateBlockchainStatus(recordId, txHash, 2);

                        log.info("溯源记录上链成功: recordId={}, txHash={}", recordId, txHash);
                        return txHash;

                    } catch (Exception e) {
                        // 更新为上链失败
                        updateBlockchainStatus(recordId, null, 3);
                        log.error("溯源记录上链失败: recordId={}", recordId, e);
                        return null;
                    }
                }).exceptionally(throwable -> {
                    log.error("异步上链异常: recordId={}", recordId, throwable);
                    updateBlockchainStatus(recordId, null, 3);
                    return null;
                });

                return recordId;

            } else {
                throw new RuntimeException("获取分布式锁超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("创建溯源记录被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<TraceRecord> getTraceChain(String traceCode) {
        if (!StringUtils.hasText(traceCode)) {
            throw new IllegalArgumentException("溯源码不能为空");
        }

        LambdaQueryWrapper<TraceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceRecord::getTraceCode, traceCode)
               .orderByAsc(TraceRecord::getRecordTime)
               .orderByAsc(TraceRecord::getCreateTime);

        List<TraceRecord> records = this.list(wrapper);
        log.info("查询溯源链: traceCode={}, recordCount={}", traceCode, records.size());

        return records;
    }

    @Override
    public Page<TraceRecord> getTraceRecordsPage(Page<TraceRecord> page, String traceCode, Long batchId, Integer recordType) {
        LambdaQueryWrapper<TraceRecord> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(traceCode)) {
            wrapper.eq(TraceRecord::getTraceCode, traceCode);
        }
        if (batchId != null) {
            wrapper.eq(TraceRecord::getBatchId, batchId);
        }
        if (recordType != null) {
            wrapper.eq(TraceRecord::getRecordType, recordType);
        }

        wrapper.orderByDesc(TraceRecord::getCreateTime);

        return this.page(page, wrapper);
    }

    @Override
    public boolean verifyTraceIntegrity(String traceCode) {
        try {
            // 查询本地最新记录
            LambdaQueryWrapper<TraceRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TraceRecord::getTraceCode, traceCode)
                   .orderByDesc(TraceRecord::getCreateTime)
                   .last("LIMIT 1");

            TraceRecord localRecord = this.getOne(wrapper);
            if (localRecord == null) {
                log.warn("本地未找到溯源记录: traceCode={}", traceCode);
                return false;
            }

            // 验证链上数据完整性
            boolean verified = blockchainService.verifyTraceIntegrity(traceCode, localRecord);
            log.info("溯源数据完整性验证结果: traceCode={}, verified={}", traceCode, verified);

            return verified;

        } catch (Exception e) {
            log.error("溯源数据完整性验证异常: traceCode={}", traceCode, e);
            return false;
        }
    }

    @Override
    public BlockchainProof getTraceProof(String traceCode) {
        try {
            BlockchainProof proof = blockchainService.generateTraceProof(traceCode);
            log.info("生成区块链证明: traceCode={}, txHash={}", traceCode, proof.getTxHash());
            return proof;
        } catch (Exception e) {
            log.error("生成区块链证明失败: traceCode={}", traceCode, e);
            throw new RuntimeException("生成区块链证明失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBlockchainStatus(Long recordId, String txHash, Integer status) {
        try {
            traceRecordMapper.updateBlockchainStatus(recordId, txHash, status);
            log.debug("更新区块链状态: recordId={}, txHash={}, status={}", recordId, txHash, status);
        } catch (Exception e) {
            log.error("更新区块链状态失败: recordId={}", recordId, e);
            throw new RuntimeException("更新区块链状态失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadSingleRecordToBlockchain(Long recordId) {
        try {
            // 1. 查询记录
            TraceRecord record = this.getById(recordId);
            if (record == null) {
                throw new RuntimeException("溯源记录不存在");
            }

            // 2. 检查是否已上链
            if (record.getBlockchainStatus() == 2) {
                log.warn("记录已上链，无需重复上链: recordId={}", recordId);
                return;
            }

            // 3. 更新状态为上链中
            updateBlockchainStatus(recordId, null, 1);

            // 4. 异步执行上链
            CompletableFuture.runAsync(() -> {
                try {
                    String txHash = blockchainService.uploadTraceRecord(record);
                    updateBlockchainStatus(recordId, txHash, 2);
                    log.info("记录上链成功: recordId={}, txHash={}", recordId, txHash);
                } catch (Exception e) {
                    updateBlockchainStatus(recordId, null, 3);
                    log.error("记录上链失败: recordId={}", recordId, e);
                }
            });

        } catch (Exception e) {
            log.error("提交上链请求失败: recordId={}", recordId, e);
            throw new RuntimeException("提交上链请求失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUploadToBlockchain(Integer limit) {
        // 查询未上链的记录
        LambdaQueryWrapper<TraceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceRecord::getBlockchainStatus, 0) // 未上链
               .orderByAsc(TraceRecord::getCreateTime);

        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        } else {
            wrapper.last("LIMIT 100"); // 默认限制100条
        }

        List<TraceRecord> records = this.list(wrapper);
        if (records.isEmpty()) {
            log.info("没有需要上链的记录");
            return 0;
        }

        int successCount = 0;
        for (TraceRecord record : records) {
            try {
                // 更新状态为上链中
                updateBlockchainStatus(record.getId(), null, 1);

                // 执行上链
                String txHash = blockchainService.uploadTraceRecord(record);

                // 更新为上链成功
                updateBlockchainStatus(record.getId(), txHash, 2);

                successCount++;
                log.info("批量上链成功: recordId={}, txHash={}", record.getId(), txHash);

            } catch (Exception e) {
                // 更新为上链失败
                updateBlockchainStatus(record.getId(), null, 3);
                log.error("批量上链失败: recordId={}", record.getId(), e);
            }
        }

        log.info("批量上链完成: total={}, success={}", records.size(), successCount);
        return successCount;
    }
}
