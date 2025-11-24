package com.linyi.cropseed.trace.module.statistics.model.vo;

import lombok.Data;

/**
 * 统计概览VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class StatisticsOverviewVO {
    private Long totalUsers;
    private Long totalOrders;
    private Double totalSales;
    private Double totalRevenue;  // 总销售额（别名）
    private Long totalInventory;
    private Long totalSeeds;
    private Long totalWarehouses;
    private Long totalCategories;
    private Long totalBatches;

    // 趋势数据（对比上期的增长率百分比）
    private Double ordersTrend;   // 订单增长趋势
    private Double revenueTrend;  // 销售额增长趋势
    private Double usersTrend;    // 用户增长趋势
    private Double seedsTrend;    // 种子品种增长趋势
}
