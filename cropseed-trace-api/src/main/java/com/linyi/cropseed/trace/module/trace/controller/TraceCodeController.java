package com.linyi.cropseed.trace.module.trace.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.trace.service.TraceCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 溯源码管理接口
 *
 * @author linyi
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/trace/codes")
@RequiredArgsConstructor
@Tag(name = "溯源码管理", description = "溯源码生成、验证和解析")
public class TraceCodeController {

    private final TraceCodeService traceCodeService;

    @Operation(summary = "生成溯源码", description = "根据地区代码生成新的溯源码")
    @PostMapping("/generate")
    public Result<String> generateTraceCode(@Parameter(description = "地区代码") @RequestParam String regionCode) {
        try {
            if (regionCode == null || regionCode.trim().isEmpty()) {
                return Result.fail("地区代码不能为空");
            }

            String traceCode = traceCodeService.generateTraceCode(regionCode.trim().toUpperCase());

            log.info("生成溯源码成功: regionCode={}, traceCode={}", regionCode, traceCode);
            return Result.success("溯源码生成成功", traceCode);

        } catch (Exception e) {
            log.error("生成溯源码失败: regionCode={}", regionCode, e);
            return Result.fail("生成失败: " + e.getMessage());
        }
    }

    @Operation(summary = "验证溯源码", description = "验证溯源码格式是否正确")
    @PostMapping("/validate")
    public Result<Boolean> validateTraceCode(@Parameter(description = "溯源码") @RequestParam String traceCode) {
        try {
            boolean valid = traceCodeService.validateTraceCode(traceCode);

            String message = valid ? "溯源码格式正确" : "溯源码格式不正确";
            log.info("验证溯源码: traceCode={}, valid={}", traceCode, valid);

            return Result.success(message, valid);

        } catch (Exception e) {
            log.error("验证溯源码失败: traceCode={}", traceCode, e);
            return Result.fail("验证失败: " + e.getMessage());
        }
    }

    @Operation(summary = "解析溯源码", description = "解析溯源码获取详细信息")
    @GetMapping("/parse/{traceCode}")
    public Result<Map<String, Object>> parseTraceCode(@Parameter(description = "溯源码") @PathVariable String traceCode) {
        try {
            TraceCodeService.TraceCodeInfo info = traceCodeService.parseTraceCode(traceCode);

            Map<String, Object> result = new HashMap<>();
            result.put("traceCode", traceCode);
            result.put("regionCode", info.getRegionCode());
            result.put("regionName", info.getRegionName());
            result.put("year", info.getYear());
            result.put("number", info.getNumber());
            result.put("valid", true);

            log.info("解析溯源码成功: traceCode={}, regionCode={}, year={}, number={}",
                    traceCode, info.getRegionCode(), info.getYear(), info.getNumber());

            return Result.success("解析成功", result);

        } catch (Exception e) {
            log.error("解析溯源码失败: traceCode={}", traceCode, e);
            return Result.fail("解析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量生成溯源码", description = "批量生成指定数量的溯源码")
    @PostMapping("/batch-generate")
    public Result<Map<String, Object>> batchGenerateTraceCodes(
            @Parameter(description = "地区代码") @RequestParam String regionCode,
            @Parameter(description = "生成数量") @RequestParam Integer count) {
        try {
            if (count == null || count <= 0 || count > 100) {
                return Result.fail("生成数量必须在1-100之间");
            }

            Map<String, Object> result = new HashMap<>();
            String[] traceCodes = new String[count];

            for (int i = 0; i < count; i++) {
                traceCodes[i] = traceCodeService.generateTraceCode(regionCode.trim().toUpperCase());
            }

            result.put("traceCodes", traceCodes);
            result.put("count", count);
            result.put("regionCode", regionCode);

            log.info("批量生成溯源码成功: regionCode={}, count={}", regionCode, count);
            return Result.success("批量生成成功", result);

        } catch (Exception e) {
            log.error("批量生成溯源码失败: regionCode={}, count={}", regionCode, count, e);
            return Result.fail("批量生成失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取地区代码列表", description = "获取支持的地区代码列表")
    @GetMapping("/regions")
    public Result<Map<String, String>> getSupportedRegions() {
        try {
            Map<String, String> regions = new HashMap<>();
            regions.put("BJ", "北京市");
            regions.put("SH", "上海市");
            regions.put("GZ", "广州市");
            regions.put("SZ", "深圳市");
            regions.put("HZ", "杭州市");
            regions.put("CD", "成都市");
            regions.put("WH", "武汉市");
            regions.put("XA", "西安市");
            regions.put("NJ", "南京市");
            regions.put("TJ", "天津市");
            regions.put("CQ", "重庆市");
            regions.put("SY", "沈阳市");
            regions.put("DL", "大连市");
            regions.put("QD", "青岛市");
            regions.put("JN", "济南市");

            return Result.success("获取成功", regions);

        } catch (Exception e) {
            log.error("获取地区代码列表失败", e);
            return Result.fail("获取失败: " + e.getMessage());
        }
    }

    @Operation(summary = "检查溯源码使用状态", description = "检查溯源码是否已被使用")
    @GetMapping("/check/{traceCode}")
    public Result<Map<String, Object>> checkTraceCodeUsage(
            @Parameter(description = "溯源码") @PathVariable String traceCode) {
        try {
            // 验证格式
            boolean valid = traceCodeService.validateTraceCode(traceCode);
            if (!valid) {
                return Result.fail("溯源码格式不正确");
            }

            // 检查是否已使用（查询是否有溯源记录）
            // TODO: 调用TraceRecordService检查是否已有记录
            boolean used = false; // 临时设为false

            Map<String, Object> result = new HashMap<>();
            result.put("traceCode", traceCode);
            result.put("valid", valid);
            result.put("used", used);
            result.put("status", used ? "已使用" : "未使用");

            log.info("检查溯源码状态: traceCode={}, used={}", traceCode, used);
            return Result.success("检查完成", result);

        } catch (Exception e) {
            log.error("检查溯源码状态失败: traceCode={}", traceCode, e);
            return Result.fail("检查失败: " + e.getMessage());
        }
    }
}
