package com.linyi.cropseed.trace.module.statistics.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.statistics.service.StatisticsService;
import com.linyi.cropseed.trace.module.statistics.model.vo.StatisticsOverviewVO;
import com.linyi.cropseed.trace.module.statistics.model.vo.StatisticsChartVO;
import com.linyi.cropseed.trace.module.statistics.model.vo.StatisticsTableVO;
import com.linyi.cropseed.trace.module.statistics.service.impl.StatisticsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计管理Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "统计管理")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取统计数据概览")
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<StatisticsOverviewVO> getStatisticsData(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        StatisticsOverviewVO result = statisticsService.getStatisticsOverview(startDate, endDate);
        return Result.success(result);
    }

    @Operation(summary = "获取图表数据")
    @GetMapping("/chart")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<StatisticsChartVO> getChartData(
            @Parameter(description = "图表类型") @RequestParam String chartType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        StatisticsChartVO result = statisticsService.getChartData(chartType, startDate, endDate);
        return Result.success(result);
    }

    @Operation(summary = "获取表格数据")
    @GetMapping("/table")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<StatisticsTableVO> getTableData(
            @Parameter(description = "表格类型") @RequestParam String tableType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "限制条数") @RequestParam(required = false) Integer limit) {
        StatisticsTableVO result = statisticsService.getTableData(tableType, startDate, endDate);
        return Result.success(result);
    }

    @Operation(summary = "获取销售趋势数据")
    @GetMapping("/sales/trend")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<List<Map<String, Object>>> getSalesTrend(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "天数") @RequestParam(required = false) Integer days) {
        List<Map<String, Object>> result = statisticsService.getSalesTrend(startDate, endDate);
        return Result.success(result);
    }

    @Operation(summary = "获取订单状态分布")
    @GetMapping("/order/status")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<List<Map<String, Object>>> getOrderStatusDistribution(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> result = statisticsService.getOrderStatusDistribution(startDate, endDate);
        return Result.success(result);
    }

    @Operation(summary = "获取用户增长数据")
    @GetMapping("/user/growth")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<List<Map<String, Object>>> getUserGrowth(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> result = statisticsService.getUserGrowth(startDate, endDate);
        return Result.success(result);
    }

    @Operation(summary = "获取种子品类销售排行")
    @GetMapping("/category/ranking")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<List<Map<String, Object>>> getCategoryRanking(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "统计类型") @RequestParam(required = false) String type) {
        List<Map<String, Object>> result;
        if ("inventory".equals(type)) {
            // 返回库存分布数据
            result = statisticsService.getInventoryStatistics();
        } else {
            // 返回品类销售排行
            result = statisticsService.getCategoryRanking(startDate, endDate);
        }
        return Result.success(result);
    }

    @Operation(summary = "获取库存统计")
    @GetMapping("/inventory")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<List<Map<String, Object>>> getInventoryStatistics(
            @Parameter(description = "统计类型") @RequestParam(required = false) String type,
            @Parameter(description = "限制条数") @RequestParam(required = false) Integer limit) {
        List<Map<String, Object>> result;
        if ("low_stock".equals(type)) {
            result = ((StatisticsServiceImpl) statisticsService)
                    .getInventoryStatistics(type, limit);
        } else {
            result = statisticsService.getInventoryStatistics();
        }
        return Result.success(result);
    }

    @Operation(summary = "获取推荐系统统计")
    @GetMapping("/recommendation")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<List<Map<String, Object>>> getRecommendationStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> result = statisticsService.getRecommendationStatistics(startDate, endDate);
        return Result.success(result);
    }

    @Operation(summary = "获取支付统计")
    @GetMapping("/payment")
    @PreAuthorize("hasAuthority('statistics:view')")
    public Result<List<Map<String, Object>>> getPaymentStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> result = statisticsService.getPaymentStatistics(startDate, endDate);
        return Result.success(result);
    }

    @Operation(summary = "导出统计数据")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('statistics:export')")
    public Result<String> exportStatisticsData(
            @Parameter(description = "导出类型") @RequestParam String exportType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        String result = statisticsService.exportStatisticsData(exportType, startDate, endDate);
        return Result.success(result);
    }
}
