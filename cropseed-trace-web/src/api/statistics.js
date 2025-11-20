import request from "./request";

// 获取统计数据
export function getStatisticsData(params) {
  return request({
    url: "/statistics/overview",
    method: "get",
    params,
  });
}

// 获取图表数据
export function getChartData(params) {
  return request({
    url: "/statistics/chart",
    method: "get",
    params,
  });
}

// 获取表格数据
export function getTableData(params) {
  return request({
    url: "/statistics/table",
    method: "get",
    params,
  });
}

// 获取销售趋势数据
export function getSalesTrend(params) {
  return request({
    url: "/statistics/sales/trend",
    method: "get",
    params,
  });
}

// 获取订单状态分布
export function getOrderStatusDistribution(params) {
  return request({
    url: "/statistics/order/status",
    method: "get",
    params,
  });
}

// 获取用户增长数据
export function getUserGrowth(params) {
  return request({
    url: "/statistics/user/growth",
    method: "get",
    params,
  });
}

// 获取种子品类销售排行
export function getCategoryRanking(params) {
  return request({
    url: "/statistics/category/ranking",
    method: "get",
    params,
  });
}

// 获取库存统计
export function getInventoryStatistics(params) {
  return request({
    url: "/statistics/inventory",
    method: "get",
    params,
  });
}

// 获取推荐系统统计
export function getRecommendationStatistics(params) {
  return request({
    url: "/statistics/recommendation",
    method: "get",
    params,
  });
}

// 获取支付统计
export function getPaymentStatistics(params) {
  return request({
    url: "/statistics/payment",
    method: "get",
    params,
  });
}

// 导出统计数据
export function exportStatisticsData(params) {
  return request({
    url: "/statistics/export",
    method: "get",
    params,
    responseType: "blob",
  });
}

// ==== 首页专用API ====

// 获取首页统计概览数据
export function getDashboardOverview() {
  return request({
    url: "/statistics/overview",
    method: "get"
  });
}

// 获取近期订单数据
export function getRecentOrders(limit = 5) {
  return request({
    url: "/statistics/table",
    method: "get",
    params: {
      tableType: "recent_orders",
      limit
    }
  });
}

// 获取库存预警数据
export function getLowStockItems(limit = 5) {
  return request({
    url: "/statistics/inventory",
    method: "get",
    params: {
      type: "low_stock",
      limit
    }
  });
}

// 获取销售趋势图表数据（近7天）
export function getDashboardSalesTrend() {
  return request({
    url: "/statistics/sales/trend",
    method: "get",
    params: {
      days: 7
    }
  });
}

// 获取库存分布图表数据
export function getDashboardInventoryDistribution() {
  return request({
    url: "/statistics/category/ranking",
    method: "get",
    params: {
      type: "inventory"
    }
  });
}
