package com.linyi.cropseed.trace.module.statistics.service;

import com.linyi.cropseed.trace.module.statistics.model.vo.StatisticsOverviewVO;
import com.linyi.cropseed.trace.module.statistics.model.vo.StatisticsChartVO;
import com.linyi.cropseed.trace.module.statistics.model.vo.StatisticsTableVO;

import java.util.List;
import java.util.Map;

/**
 * 统计Service接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface StatisticsService {

    /**
     * 获取统计数据概览
     */
    StatisticsOverviewVO getStatisticsOverview(String startDate, String endDate);

    /**
     * 获取图表数据
     */
    StatisticsChartVO getChartData(String chartType, String startDate, String endDate);

    /**
     * 获取表格数据
     */
    StatisticsTableVO getTableData(String tableType, String startDate, String endDate);

    /**
     * 获取销售趋势数据
     */
    List<Map<String, Object>> getSalesTrend(String startDate, String endDate);

    /**
     * 获取订单状态分布
     */
    List<Map<String, Object>> getOrderStatusDistribution(String startDate, String endDate);

    /**
     * 获取用户增长数据
     */
    List<Map<String, Object>> getUserGrowth(String startDate, String endDate);

    /**
     * 获取种子品类销售排行
     */
    List<Map<String, Object>> getCategoryRanking(String startDate, String endDate);

    /**
     * 获取库存统计
     */
    List<Map<String, Object>> getInventoryStatistics();

    /**
     * 获取推荐系统统计
     */
    List<Map<String, Object>> getRecommendationStatistics(String startDate, String endDate);

    /**
     * 获取支付统计
     */
    List<Map<String, Object>> getPaymentStatistics(String startDate, String endDate);

    /**
     * 导出统计数据
     */
    String exportStatisticsData(String exportType, String startDate, String endDate);
}
