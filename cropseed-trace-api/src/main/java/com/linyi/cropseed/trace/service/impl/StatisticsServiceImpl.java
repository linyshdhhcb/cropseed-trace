package com.linyi.cropseed.trace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linyi.cropseed.trace.entity.*;
import com.linyi.cropseed.trace.mapper.*;
import com.linyi.cropseed.trace.service.StatisticsService;
import com.linyi.cropseed.trace.vo.StatisticsChartVO;
import com.linyi.cropseed.trace.vo.StatisticsOverviewVO;
import com.linyi.cropseed.trace.vo.StatisticsTableVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现类
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
    private final SeedInfoMapper seedInfoMapper;
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

        // 种子统计 - 查询种子信息表的总数
        try {
            Long totalSeeds = seedInfoMapper.selectCount(null);
            vo.setTotalSeeds(totalSeeds != null ? totalSeeds : 0L);
        } catch (Exception e) {
            log.warn("获取种子统计失败", e);
            vo.setTotalSeeds(0L);
        }

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
            case "recent_orders":
                vo.setData(getRecentOrdersData());
                vo.setTitle("最新订单");
                break;
            case "category_ranking":
                vo.setData(getCategoryRankingData(startDate, endDate));
                vo.setTitle("品类销售排行");
                break;
            case "inventory_stats":
                vo.setData(getInventoryStatsData());
                vo.setTitle("库存统计");
                break;
            case "recommendation_stats":
                vo.setData(getRecommendationStatsData());
                vo.setTitle("推荐系统统计");
                break;
            case "payment_stats":
                vo.setData(getPaymentStatsData(startDate, endDate));
                vo.setTitle("支付统计");
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

    /**
     * 获取库存统计（支持传参）
     */
    public List<Map<String, Object>> getInventoryStatistics(String type, Integer limit) {
        if ("low_stock".equals(type)) {
            return getLowStockItemsData(limit);
        }
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
        // 数据导出功能
        try {
            List<Map<String, Object>> data = getTableData(exportType, startDate, endDate).getData();
            String fileName = exportType + "_" + System.currentTimeMillis() + ".csv";
            
            // 简化导出功能，返回文件名
            log.info("导出数据: {} 条记录", data.size());
            return fileName;
        } catch (Exception e) {
            log.error("数据导出失败", e);
            throw new RuntimeException("数据导出失败");
        }
    }

    // 辅助方法
    private List<Map<String, Object>> getSalesTrendData(String startDate, String endDate) {
        // 销售趋势数据实现 - 近7天销售数据
        List<Map<String, Object>> trendData = new ArrayList<>();

        try {
            // 如果没有指定日期范围，默认使用近7天
            LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderInfo::getOrderStatus, 4); // 已完成订单

            if (!StringUtils.hasText(startDate) || !StringUtils.hasText(endDate)) {
                // 近7天数据
                for (int i = 6; i >= 0; i--) {
                    String date = java.time.LocalDate.now().minusDays(i).toString();

                    // 查询当天的销售金额
                    LambdaQueryWrapper<OrderInfo> dailyWrapper = new LambdaQueryWrapper<>();
                    dailyWrapper.eq(OrderInfo::getOrderStatus, 4)
                               .likeRight(OrderInfo::getCreateTime, date);

                    List<OrderInfo> dailyOrders = orderInfoMapper.selectList(dailyWrapper);
                    Double dailySales = dailyOrders.stream()
                            .mapToDouble(order -> order.getPaidAmount() != null ? order.getPaidAmount().doubleValue() : 0.0)
                            .sum();

                    trendData.add(Map.of(
                        "date", date.substring(5), // MM-dd格式
                        "day", getDayOfWeek(i),
                        "sales", dailySales,
                        "amount", dailySales
                    ));
                }
            } else {
                // 使用指定的日期范围
                wrapper.between(OrderInfo::getCreateTime, startDate, endDate);
                List<OrderInfo> orders = orderInfoMapper.selectList(wrapper);
                Double totalSales = orders.stream()
                        .mapToDouble(order -> order.getPaidAmount() != null ? order.getPaidAmount().doubleValue() : 0.0)
                        .sum();

                trendData.add(Map.of(
                    "date", startDate + " - " + endDate,
                    "sales", totalSales,
                    "amount", totalSales
                ));
            }
        } catch (Exception e) {
            log.error("获取销售趋势数据失败", e);
            // 返回空数据
            trendData = new ArrayList<>();
        }

        return trendData;
    }

    private String getDayOfWeek(int daysAgo) {
        String[] days = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        int dayOfWeek = java.time.LocalDate.now().minusDays(daysAgo).getDayOfWeek().getValue() % 7;
        return days[dayOfWeek];
    }

    private List<Map<String, Object>> getRecentOrdersData() {
        // 获取最近的订单数据
        try {
            LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(OrderInfo::getCreateTime)
                   .last("LIMIT 5"); // 限制最新5条

            List<OrderInfo> orders = orderInfoMapper.selectList(wrapper);

            return orders.stream().map(order -> {
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("orderNo", order.getOrderNo());
                orderData.put("order_no", order.getOrderNo());
                orderData.put("customerName", "用户" + (order.getUserId() != null ? order.getUserId() : "N/A"));
                orderData.put("customer_name", "用户" + (order.getUserId() != null ? order.getUserId() : "N/A"));
                orderData.put("amount", order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0);
                orderData.put("paid_amount", order.getPaidAmount() != null ? order.getPaidAmount().doubleValue() : 0.0);
                orderData.put("status", order.getOrderStatus());
                orderData.put("order_status", order.getOrderStatus());
                orderData.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString().substring(0, 10) : "");
                orderData.put("create_time", order.getCreateTime() != null ? order.getCreateTime().toString().substring(0, 10) : "");
                return orderData;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取最近订单数据失败", e);
            // 返回空数据
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getLowStockItemsData(Integer limit) {
        // 获取库存预警数据 - 根据库存表的min_stock字段进行预警
        try {
            int limitCount = limit != null ? limit : 5;

            // 查询库存数量小于最低库存预警的记录
            // 使用自定义SQL查询以关联种子信息
            List<Map<String, Object>> lowStockItems = inventoryMapper.selectLowStockItems(limitCount);
            
            if (lowStockItems == null || lowStockItems.isEmpty()) {
                // 如果自定义查询不可用，使用基础查询
                LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
                // 查询quantity < min_stock 且 min_stock > 0 的记录
                wrapper.apply("quantity < min_stock")
                       .gt(Inventory::getMinStock, 0) // 只查询设置了预警值的库存
                       .orderByAsc(Inventory::getQuantity)
                       .last("LIMIT " + limitCount);

                List<Inventory> inventories = inventoryMapper.selectList(wrapper);
                
                if (inventories.isEmpty()) {
                    return new ArrayList<>();
                }

                return inventories.stream().map(inventory -> {
                    Map<String, Object> itemData = new HashMap<>();
                    
                    // 尝试获取种子名称
                    String seedName = "种子ID-" + inventory.getSeedId();
                    try {
                        SeedInfo seedInfo = seedInfoMapper.selectById(inventory.getSeedId());
                        if (seedInfo != null) {
                            seedName = seedInfo.getSeedName();
                        }
                    } catch (Exception e) {
                        log.warn("获取种子信息失败，seedId: {}", inventory.getSeedId());
                    }
                    
                    itemData.put("seedName", seedName);
                    itemData.put("seed_name", seedName);
                    itemData.put("productName", seedName);
                    itemData.put("currentStock", inventory.getQuantity());
                    itemData.put("current_stock", inventory.getQuantity());
                    itemData.put("quantity", inventory.getQuantity());
                    itemData.put("minStock", inventory.getMinStock() != null ? inventory.getMinStock() : 0);
                    itemData.put("min_stock", inventory.getMinStock() != null ? inventory.getMinStock() : 0);
                    itemData.put("safetyStock", inventory.getMinStock() != null ? inventory.getMinStock() : 0);
                    itemData.put("warehouseId", inventory.getWarehouseId());
                    itemData.put("batchId", inventory.getBatchId());
                    return itemData;
                }).collect(Collectors.toList());
            }
            
            return lowStockItems;

        } catch (Exception e) {
            log.error("获取库存预警数据失败", e);
            // 返回空数据
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getOrderStatusData(String startDate, String endDate) {
        // 订单状态分布数据实现
        try {
            List<Map<String, Object>> statusData = new ArrayList<>();

            // 定义订单状态映射
            Map<Integer, String> statusMap = Map.of(
                1, "待支付",
                2, "已支付",
                3, "已发货",
                4, "已完成",
                5, "已取消"
            );

            for (Map.Entry<Integer, String> entry : statusMap.entrySet()) {
                LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(OrderInfo::getOrderStatus, entry.getKey());

                if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
                    wrapper.between(OrderInfo::getCreateTime, startDate, endDate);
                }

                Long count = orderInfoMapper.selectCount(wrapper);
                statusData.add(Map.of(
                    "status", entry.getValue(),
                    "count", count,
                    "value", count
                ));
            }

            return statusData;
        } catch (Exception e) {
            log.error("获取订单状态分布数据失败", e);
            // 返回空数据
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getUserGrowthData(String startDate, String endDate) {
        // 用户增长数据实现
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getCategoryRankingData(String startDate, String endDate) {
        // 品类销售排行数据实现
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getInventoryStatsData() {
        // 库存统计数据实现 - 按种子分类统计库存分布
        try {
            List<Map<String, Object>> inventoryData = new ArrayList<>();

            // 通过种子分类统计库存分布
            List<SeedCategory> categories = seedCategoryMapper.selectList(null);

            for (SeedCategory category : categories) {
                // 统计该分类下的总库存
                Long totalQuantity = 0L;
                
                try {
                    // 尝试使用优化查询
                    totalQuantity = inventoryMapper.selectInventoryQuantityByCategory(category.getId());
                } catch (Exception e) {
                    log.debug("优化查询失败，使用基础查询");
                }
                
                if (totalQuantity == null || totalQuantity == 0) {
                    // 使用基础查询：先查询该分类下的所有种子ID
                    try {
                        LambdaQueryWrapper<SeedInfo> seedWrapper = new LambdaQueryWrapper<>();
                        seedWrapper.eq(SeedInfo::getCategoryId, category.getId());
                        List<SeedInfo> seeds = seedInfoMapper.selectList(seedWrapper);
                        
                        if (!seeds.isEmpty()) {
                            // 获取种子ID列表
                            List<Long> seedIds = seeds.stream()
                                    .map(SeedInfo::getId)
                                    .collect(Collectors.toList());
                            
                            // 查询这些种子的库存总量
                            LambdaQueryWrapper<Inventory> invWrapper = new LambdaQueryWrapper<>();
                            invWrapper.in(Inventory::getSeedId, seedIds)
                                     .gt(Inventory::getQuantity, 0); // 只统计有库存的
                            
                            List<Inventory> inventories = inventoryMapper.selectList(invWrapper);
                            totalQuantity = inventories.stream()
                                    .mapToLong(inv -> inv.getQuantity() != null ? inv.getQuantity() : 0L)
                                    .sum();
                        } else {
                            totalQuantity = 0L;
                        }
                    } catch (Exception e) {
                        log.warn("查询分类{}库存失败", category.getCategoryName(), e);
                        totalQuantity = 0L;
                    }
                }

                // 只添加有库存的分类
                if (totalQuantity > 0) {
                    inventoryData.add(Map.of(
                        "name", category.getCategoryName(),
                        "category", category.getCategoryName(),
                        "seed_name", category.getCategoryName(),
                        "quantity", totalQuantity,
                        "count", totalQuantity,
                        "value", totalQuantity
                    ));
                }
            }

            return inventoryData;
        } catch (Exception e) {
            log.error("获取库存统计数据失败", e);
            // 返回空数据
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> getRecommendationStatsData() {
        // 推荐系统统计数据实现
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getPaymentStatsData(String startDate, String endDate) {
        // 支付统计数据实现
        return new ArrayList<>();
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
