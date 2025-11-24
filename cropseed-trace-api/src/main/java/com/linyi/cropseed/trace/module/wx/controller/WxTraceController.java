package com.linyi.cropseed.trace.module.wx.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.trace.model.vo.TraceChainData;
import com.linyi.cropseed.trace.module.trace.model.entity.TraceRecord;
import com.linyi.cropseed.trace.module.trace.service.TraceRecordService;
import com.linyi.cropseed.trace.module.trace.service.BlockchainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 微信小程序溯源查询
 *
 * @author linyi
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "微信溯源", description = "微信小程序溯源查询接口")
@RestController
@RequestMapping("/wx/trace")
@RequiredArgsConstructor
public class WxTraceController {

    private final TraceRecordService traceRecordService;
    private final BlockchainService blockchainService;

    @Operation(summary = "扫码查询溯源信息", description = "通过扫描二维码获取溯源码并查询完整溯源信息")
    @GetMapping("/scan/{traceCode}")
    public Result<Map<String, Object>> scanTraceCode(
            @Parameter(description = "溯源码") @PathVariable String traceCode,
            @Parameter(description = "微信openid") @RequestParam(required = false) String openid) {

        log.info("扫码查询溯源信息: traceCode={}, openid={}", traceCode, openid);

        try {
            if (!StringUtils.hasText(traceCode)) {
                return Result.fail("溯源码不能为空");
            }

            // 查询溯源链
            List<TraceRecord> traceRecords = traceRecordService.getTraceChain(traceCode);
            if (traceRecords.isEmpty()) {
                return Result.fail("溯源码不存在，请检查输入是否正确");
            }

            // 查询链上数据
            TraceChainData chainData = null;
            try {
                chainData = blockchainService.queryChainTraceRecord(traceCode);
            } catch (Exception e) {
                log.warn("查询链上数据失败: traceCode={}, error={}", traceCode, e.getMessage());
            }

            // 组装返回数据
            Map<String, Object> result = buildTraceResult(traceCode, traceRecords, chainData);

            log.info("扫码查询成功: traceCode={}, recordCount={}", traceCode, traceRecords.size());
            return Result.success("查询成功", result);

        } catch (Exception e) {
            log.error("扫码查询失败: traceCode={}", traceCode, e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "手动输入查询溯源信息", description = "通过手动输入溯源码查询完整溯源信息")
    @GetMapping("/query/{traceCode}")
    public Result<Map<String, Object>> queryTraceCode(
            @Parameter(description = "溯源码") @PathVariable String traceCode,
            @Parameter(description = "微信openid") @RequestParam(required = false) String openid) {

        log.info("手动查询溯源信息: traceCode={}, openid={}", traceCode, openid);

        try {
            if (!StringUtils.hasText(traceCode)) {
                return Result.fail("溯源码不能为空");
            }

            // 清理溯源码（去除空格等）
            traceCode = traceCode.trim().toUpperCase();

            // 查询溯源链
            List<TraceRecord> traceRecords = traceRecordService.getTraceChain(traceCode);
            if (traceRecords.isEmpty()) {
                return Result.fail("溯源码不存在，请检查输入是否正确");
            }

            // 查询链上数据
            TraceChainData chainData = null;
            try {
                chainData = blockchainService.queryChainTraceRecord(traceCode);
            } catch (Exception e) {
                log.warn("查询链上数据失败: traceCode={}, error={}", traceCode, e.getMessage());
            }

            // 组装返回数据
            Map<String, Object> result = buildTraceResult(traceCode, traceRecords, chainData);

            log.info("手动查询成功: traceCode={}, recordCount={}", traceCode, traceRecords.size());
            return Result.success("查询成功", result);

        } catch (Exception e) {
            log.error("手动查询失败: traceCode={}", traceCode, e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "验证溯源码", description = "验证溯源码是否存在及有效性")
    @GetMapping("/verify/{traceCode}")
    public Result<Map<String, Object>> verifyTraceCode(
            @Parameter(description = "溯源码") @PathVariable String traceCode) {

        try {
            if (!StringUtils.hasText(traceCode)) {
                return Result.fail("溯源码不能为空");
            }

            traceCode = traceCode.trim().toUpperCase();

            List<TraceRecord> traceRecords = traceRecordService.getTraceChain(traceCode);

            Map<String, Object> result = new HashMap<>();
            result.put("traceCode", traceCode);
            result.put("exists", !traceRecords.isEmpty());
            result.put("recordCount", traceRecords.size());

            if (!traceRecords.isEmpty()) {
                TraceRecord firstRecord = traceRecords.get(0);
                result.put("batchId", firstRecord.getBatchId());
                result.put("recordCount", traceRecords.size());
            }

            return Result.success(result);

        } catch (Exception e) {
            log.error("验证溯源码失败: traceCode={}", traceCode, e);
            return Result.fail("验证失败: " + e.getMessage());
        }
    }

    /**
     * 构建溯源查询结果
     */
    private Map<String, Object> buildTraceResult(String traceCode, List<TraceRecord> traceRecords, TraceChainData chainData) {
        Map<String, Object> result = new HashMap<>();

        // 基本信息
        result.put("traceCode", traceCode);
        result.put("queryTime", LocalDateTime.now());
        result.put("recordCount", traceRecords.size());

        // 产品信息（从第一条记录获取）
        if (!traceRecords.isEmpty()) {
            TraceRecord firstRecord = traceRecords.get(0);
            Map<String, Object> productInfo = new HashMap<>();
            productInfo.put("batchId", firstRecord.getBatchId());
            productInfo.put("entityName", firstRecord.getEntityName());
            result.put("productInfo", productInfo);
        }

        // 溯源链（按时间正序）
        List<Map<String, Object>> traceChain = traceRecords.stream()
                .map(record -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", record.getId());
                    node.put("stage", record.getRecordStage());
                    node.put("stageName", getStageName(record.getRecordStage()));
                    node.put("content", record.getContentSummary());
                    node.put("detailedContent", record.getDetailedContent());
                    node.put("operatorName", record.getOperatorName());
                    node.put("entityName", record.getEntityName());
                    node.put("recordTime", record.getRecordTime());
                    node.put("location", record.getLocation());
                    node.put("temperature", record.getTemperature());
                    node.put("humidity", record.getHumidity());
                    node.put("quantity", record.getQuantity());
                    node.put("unit", record.getUnit());
                    node.put("qualityGrade", record.getQualityGrade());
                    node.put("images", record.getImageUrls() != null && !record.getImageUrls().isEmpty() ?
                            List.of(record.getImageUrls().split(",")) : List.of());
                    node.put("onChain", record.getBlockchainStatus() != null && record.getBlockchainStatus() == 2);
                    return node;
                })
                .collect(Collectors.toList());
        result.put("traceChain", traceChain);

        // 区块链验证信息
        Map<String, Object> blockchainInfo = new HashMap<>();
        blockchainInfo.put("verified", chainData != null);
        if (chainData != null) {
            blockchainInfo.put("txId", chainData.getTxId());
            blockchainInfo.put("timestamp", chainData.getTimestamp());
        }
        result.put("blockchainInfo", blockchainInfo);

        // 阶段统计
        Map<String, Long> stageCount = traceRecords.stream()
                .collect(Collectors.groupingBy(TraceRecord::getRecordStage, Collectors.counting()));
        result.put("stageCount", stageCount);

        return result;
    }

    /**
     * 获取阶段名称
     */
    private String getStageName(String stage) {
        Map<String, String> stageNames = new HashMap<>();
        stageNames.put("PLANTING", "种植");
        stageNames.put("GROWING", "生长");
        stageNames.put("HARVESTING", "收获");
        stageNames.put("PROCESSING", "加工");
        stageNames.put("PACKAGING", "包装");
        stageNames.put("WAREHOUSING", "入库");
        stageNames.put("SHIPPING", "运输");
        stageNames.put("QUALITY_CHECK", "质检");
        stageNames.put("SALES", "销售");
        return stageNames.getOrDefault(stage, stage);
    }
}
