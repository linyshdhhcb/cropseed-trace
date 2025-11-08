package com.linyi.cropseed.trace.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linyi.cropseed.trace.entity.*;
import com.linyi.cropseed.trace.mapper.*;
import com.linyi.cropseed.trace.service.StatisticsService;
import com.linyi.cropseed.trace.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计Service实现类
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final WxUserMapper wxUserMapper;
    private final OrderInfoMapper orderInfoMapper;
    private final InventoryMapper inventoryMapper;
    private final SeedCategoryMapper seedCategoryMapper;
    private final RecommendationMapper recommendationMapper;

    @Override
    public StatisticsOverviewVO getStatisticsOverview(String startDate, String endDate) {
        // 实现统计数据概览
        StatisticsOverviewVO vo = new StatisticsOverviewVO();

        // 用户统计
        Long totalUsers = wxUserMapper.selectCount(null);
        vo.setTotalUsers(totalUsers);

        // 订单统计
        LambdaQueryWrapper<OrderInfo> orderWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            orderWrapper.between(OrderInfo::getCreateTime, startDate, endDate);
        }
        Long totalOrders = orderInfoMapper.selectCount(orderWrapper);
        vo.setTotalOrders(totalOrders);

        // 销售统计
        LambdaQueryWrapper<OrderInfo> salesWrapper = new LambdaQueryWrapper<>();
        salesWrapper.eq(OrderInfo::getOrderStatus, 4); // 已完成订单
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            salesWrapper.between(OrderInfo::getCreateTime, startDate, endDate);
        }
        List<OrderInfo> completedOrders = orderInfoMapper.selectList(salesWrapper);
        Double totalSales = completedOrders.stream()
                .mapToDouble(order -> order.getPaidAmount() != null ? order.getPaidAmount().doubleValue() : 0.0)
                .sum();
        vo.setTotalSales(totalSales);

        // 库存统计
        Long totalInventory = inventoryMapper.selectCount(null);
        vo.setTotalInventory(totalInventory);

        return vo;
    }

    @Override
    public StatisticsChartVO getChartData(String chartType, String startDate, String endDate) {
        // 实现图表数据
        StatisticsChartVO vo = new StatisticsChartVO();
        vo.setChartType(chartType);

        switch (chartType) {
            case "sales_trend":
                vo.setData(getSalesTrendData(startDate, endDate));
                break;
            case "order_status":
                vo.setData(getOrderStatusData(startDate, endDate));
                break;
            case "user_growth":
                vo.setData(getUserGrowthData(startDate, endDate));
                break;
            default:
                vo.setData(List.of());
        }

        return vo;
    }

    @Override
    public StatisticsTableVO getTableData(String tableType, String startDate, String endDate) {
        // 实现表格数据
        StatisticsTableVO vo = new StatisticsTableVO();
        vo.setTableType(tableType);

        switch (tableType) {
            case "category_ranking":
                vo.setData(getCategoryRankingData(startDate, endDate));
                break;
            case "inventory_stats":
                vo.setData(getInventoryStatsData());
                break;
            case "recommendation_stats":
                vo.setData(getRecommendationStatsData());
                break;
            case "payment_stats":
                vo.setData(getPaymentStatsData(startDate, endDate));
                break;
            default:
                vo.setData(List.of());
        }

        return vo;
    }

    @Override
    public List<Map<String, Object>> getSalesTrend(String startDate, String endDate) {
        // 实现销售趋势数据
        return getSalesTrendData(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getOrderStatusDistribution(String startDate, String endDate) {
        // 实现订单状态分布
        return getOrderStatusData(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getUserGrowth(String startDate, String endDate) {
        // 实现用户增长数据
        return getUserGrowthData(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getCategoryRanking(String startDate, String endDate) {
        // 实现品类销售排行
        return getCategoryRankingData(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getInventoryStatistics() {
        // 实现库存统计
        return getInventoryStatsData();
    }

    @Override
    public List<Map<String, Object>> getRecommendationStatistics(String startDate, String endDate) {
        // 实现推荐系统统计
        return getRecommendationStatsData();
    }

    @Override
    public List<Map<String, Object>> getPaymentStatistics(String startDate, String endDate) {
        // 实现支付统计
        return getPaymentStatsData(startDate, endDate);
    }

    @Override
    public String exportStatisticsData(String exportType, String startDate, String endDate) {
        // 实现数据导出
        try {
            List<Map<String, Object>> data = getTableData(exportType, startDate, endDate).getData();
            String fileName = exportType + "_" + System.currentTimeMillis() + ".xlsx";
            String filePath = "/tmp/" + fileName;

            // 使用EasyExcel导出数据
            EasyExcel.write(filePath)
                    .head(getHeaders(exportType))
                    .sheet("数据导出")
                    .doWrite(data);

            return fileName;
        } catch (Exception e) {
            log.error("数据导出失败", e);
            throw new RuntimeException("数据导出失败");
        }
    }

    // 辅助方法
    private List<Map<String, Object>> getSalesTrendData(String startDate, String endDate) {
        // 销售趋势数据实现
        return List.of(Map.of("date", "2025-10-25", "sales", 1000.0));
    }

    private List<Map<String, Object>> getOrderStatusData(String startDate, String endDate) {
        // 订单状态分布数据实现
        return List.of(
                Map.of("status", "待付款", "count", 10),
                Map.of("status", "已付款", "count", 20),
                Map.of("status", "已完成", "count", 30));
    }

    private List<Map<String, Object>> getUserGrowthData(String startDate, String endDate) {
        // 用户增长数据实现
        return List.of(Map.of("date", "2025-10-25", "users", 100));
    }

    private List<Map<String, Object>> getCategoryRankingData(String startDate, String endDate) {
        // 品类销售排行数据实现
        return List.of(Map.of("category", "玉米", "sales", 5000.0));
    }

    private List<Map<String, Object>> getInventoryStatsData() {
        // 库存统计数据实现
        return List.of(Map.of("warehouse", "仓库1", "quantity", 1000));
    }

    private List<Map<String, Object>> getRecommendationStatsData() {
        // 推荐系统统计数据实现
        return List.of(Map.of("type", "协同过滤", "count", 100));
    }

    private List<Map<String, Object>> getPaymentStatsData(String startDate, String endDate) {
        // 支付统计数据实现
        return List.of(Map.of("method", "微信支付", "amount", 10000.0));
    }

    private List<List<String>> getHeaders(String dataType) {
        // 根据数据类型返回表头
        switch (dataType) {
            case "category_ranking":
                return List.of(List.of("品类名称", "销售金额", "销售数量"));
            case "inventory_stats":
                return List.of(List.of("仓库名称", "库存数量", "可用数量"));
            default:
                return List.of(List.of("数据"));
        }
    }
}
