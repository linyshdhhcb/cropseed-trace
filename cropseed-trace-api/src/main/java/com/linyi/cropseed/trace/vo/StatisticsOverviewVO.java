package com.linyi.cropseed.trace.vo;

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
    private Long totalInventory;
    private Long totalSeeds;
    private Long totalWarehouses;
    private Long totalCategories;
    private Long totalBatches;
}
