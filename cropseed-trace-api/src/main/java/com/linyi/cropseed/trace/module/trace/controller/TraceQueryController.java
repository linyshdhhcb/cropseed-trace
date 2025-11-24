package com.linyi.cropseed.trace.module.trace.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.trace.model.vo.TraceChainData;
import com.linyi.cropseed.trace.module.trace.model.entity.TraceQuery;
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

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 溯源查询接口（面向消费者）
 *
 * @author linyi
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/trace/query")
@RequiredArgsConstructor
@Tag(name = "溯源查询", description = "消费者溯源查询相关接口")
public class TraceQueryController {

    private final TraceRecordService traceRecordService;
    private final BlockchainService blockchainService;

    @Operation(summary = "溯源查询", description = "根据溯源码查询产品的完整溯源信息")
    @GetMapping("/{traceCode}")
    public Result<Map<String, Object>> queryTraceInfo(
            @Parameter(description = "溯源码") @PathVariable String traceCode,
            @Parameter(description = "查询渠道") @RequestParam(defaultValue = "3") Integer channel,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "微信openid") @RequestParam(required = false) String openid,
            HttpServletRequest request) {

        long startTime = System.currentTimeMillis();

        try {

            if (!StringUtils.hasText(traceCode)) {
                return Result.fail("溯源码不能为空");
            }

            //  查询本地溯源记录
            List<TraceRecord> traceRecords = traceRecordService.getTraceChain(traceCode);
            if (traceRecords.isEmpty()) {
                recordQuery(traceCode, userId, openid, channel, request, 2, startTime); // 溯源码不存在
                return Result.fail("溯源码不存在，请检查输入是否正确");
            }

            // 查询链上数据
            TraceChainData chainData = null;
            try {
                chainData = blockchainService.queryChainTraceRecord(traceCode);
            } catch (Exception e) {
                log.warn("查询链上数据失败: traceCode={}, error={}", traceCode, e.getMessage());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("traceCode", traceCode);
            result.put("traceRecords", traceRecords);
            result.put("chainData", chainData);
            result.put("verified", chainData != null);
            result.put("recordCount", traceRecords.size());
            result.put("queryTime", LocalDateTime.now());

            recordQuery(traceCode, userId, openid, channel, request, 1, startTime); // 查询成功

            log.info("溯源查询成功: traceCode={}, recordCount={}, hasChainData={}",
                traceCode, traceRecords.size(), chainData != null);

            return Result.success("查询成功", result);

        } catch (Exception e) {
            recordQuery(traceCode, userId, openid, channel, request, 4, startTime); // 系统错误
            log.error("溯源查询失败: traceCode={}", traceCode, e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "简化溯源查询", description = "返回简化的溯源信息，适用于小程序展示")
    @GetMapping("/simple/{traceCode}")
    public Result<Map<String, Object>> querySimpleTraceInfo(
            @Parameter(description = "溯源码") @PathVariable String traceCode,
            HttpServletRequest request) {

        try {

            List<TraceRecord> traceRecords = traceRecordService.getTraceChain(traceCode);
            if (traceRecords.isEmpty()) {
                return Result.fail("溯源码不存在");
            }

            Map<String, Object> result = new HashMap<>();

            TraceRecord firstRecord = traceRecords.get(0);
            result.put("traceCode", traceCode);
            result.put("batchId", firstRecord.getBatchId());
            result.put("productName", ""); // 可从批次信息中获取

            Map<String, Integer> stageCount = new HashMap<>();
            for (TraceRecord record : traceRecords) {
                String stage = record.getRecordStage();
                stageCount.put(stage, stageCount.getOrDefault(stage, 0) + 1);
            }
            result.put("stageCount", stageCount);
            result.put("totalRecords", traceRecords.size());

            // 最新记录
            TraceRecord latestRecord = traceRecords.get(traceRecords.size() - 1);
            result.put("latestStage", latestRecord.getRecordStage());
            result.put("latestTime", latestRecord.getRecordTime());
            result.put("latestOperator", latestRecord.getOperatorName());

            // 验证状态
            boolean verified = traceRecordService.verifyTraceIntegrity(traceCode);
            result.put("verified", verified);

            log.info("简化溯源查询成功: traceCode={}", traceCode);
            return Result.success(result);

        } catch (Exception e) {
            log.error("简化溯源查询失败: traceCode={}", traceCode, e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量查询", description = "批量查询多个溯源码信息")
    @PostMapping("/batch")
    public Result<Map<String, Object>> batchQueryTraceInfo(@Parameter(description = "溯源码列表") @RequestBody List<String> traceCodes, HttpServletRequest request) {

        try {
            if (traceCodes == null || traceCodes.isEmpty()) {
                return Result.fail("溯源码列表不能为空");
            }

            if (traceCodes.size() > 50) {
                return Result.fail("批量查询数量不能超过50个");
            }

            Map<String, Object> result = new HashMap<>();
            Map<String, List<TraceRecord>> successResults = new HashMap<>();
            Map<String, String> failResults = new HashMap<>();

            for (String traceCode : traceCodes) {
                try {
                    List<TraceRecord> records = traceRecordService.getTraceChain(traceCode);
                    if (!records.isEmpty()) {
                        successResults.put(traceCode, records);
                    } else {
                        failResults.put(traceCode, "溯源码不存在");
                    }
                } catch (Exception e) {
                    failResults.put(traceCode, e.getMessage());
                    log.warn("批量查询单个溯源码失败: traceCode={}, error={}", traceCode, e.getMessage());
                }
            }

            result.put("success", successResults);
            result.put("failed", failResults);
            result.put("total", traceCodes.size());
            result.put("successCount", successResults.size());
            result.put("failCount", failResults.size());

            log.info("批量溯源查询完成: total={}, success={}, fail={}",
                traceCodes.size(), successResults.size(), failResults.size());

            return Result.success(result);

        } catch (Exception e) {
            log.error("批量溯源查询失败", e);
            return Result.fail("批量查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询统计信息", description = "获取溯源查询的统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getQueryStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {

        try {
            // TODO: 实现查询统计逻辑
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalQueries", 0);
            statistics.put("successQueries", 0);
            statistics.put("failQueries", 0);
            statistics.put("uniqueTraceCodes", 0);
            statistics.put("popularTraceCodes", List.of());

            return Result.success(statistics);

        } catch (Exception e) {
            log.error("获取查询统计信息失败", e);
            return Result.fail("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 记录查询日志
     */
    private void recordQuery(String traceCode, Long userId, String openid, Integer channel,
                           HttpServletRequest request, Integer result, long startTime) {
        try {
            TraceQuery query = new TraceQuery();
            query.setTraceCode(traceCode);
            query.setQueryUserId(userId);
            query.setQueryOpenid(openid);
            query.setQueryIp(getClientIp(request));
            query.setQueryDevice(request.getHeader("User-Agent"));
            query.setQueryChannel(channel);
            query.setQueryTime(LocalDateTime.now());
            query.setQueryResult(result);
            query.setResponseTime((int) (System.currentTimeMillis() - startTime));

            // TODO: 保存查询记录到数据库

        } catch (Exception e) {
            log.warn("记录查询日志失败: traceCode={}", traceCode, e);
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xip = request.getHeader("X-Real-IP");
        String xfor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xfor) && !"unKnown".equalsIgnoreCase(xfor)) {
            int index = xfor.indexOf(",");
            if (index != -1) {
                return xfor.substring(0, index);
            } else {
                return xfor;
            }
        }
        xfor = xip;
        if (StringUtils.hasText(xfor) && !"unKnown".equalsIgnoreCase(xfor)) {
            return xfor;
        }
        if (StringUtils.hasText(xfor = request.getHeader("Proxy-Client-IP")) && !"unKnown".equalsIgnoreCase(xfor)) {
            return xfor;
        }
        if (StringUtils.hasText(xfor = request.getHeader("WL-Proxy-Client-IP")) && !"unKnown".equalsIgnoreCase(xfor)) {
            return xfor;
        }
        return request.getRemoteAddr();
    }
}
