import request from "./request";

// 订单管理
export function getOrderList(params) {
  return request({
    url: "/order/list/page",
    method: "get",
    params,
  });
}

export function getOrderDetail(id) {
  return request({
    url: `/order/list/${id}`,
    method: "get",
  });
}

// 审核订单 - 根据后端接口使用audit路径
export function auditOrder(id, approved, remark) {
  return request({
    url: `/order/list/${id}/audit`,
    method: "post",
    params: { approved, remark },
  });
}

// 支付订单
export function payOrder(id, paymentMethod) {
  return request({
    url: `/order/list/${id}/pay`,
    method: "post",
    params: { paymentMethod },
  });
}

// 通用状态更新（如果需要的话）
export function updateOrderStatus(id, status) {
  // 这个方法根据状态调用对应的具体方法
  switch(status) {
    case 2: // 审核通过
      return auditOrder(id, true);
    case 5: // 审核拒绝
      return auditOrder(id, false);
    default:
      return Promise.reject(new Error('不支持的状态更新'));
  }
}

export function cancelOrder(id, reason) {
  return request({
    url: `/order/list/${id}/cancel`,
    method: "post",
    params: { reason },
  });
}

export function shipOrder(id, logisticsCompany, trackingNumber, remark) {
  return request({
    url: `/order/list/${id}/ship`,
    method: "post",
    params: { logisticsCompany, trackingNumber, remark },
  });
}

export function completeOrder(id) {
  return request({
    url: `/order/list/${id}/complete`,
    method: "post",
  });
}

// 退款相关
export function refundOrder(id, reason) {
  return request({
    url: `/order/list/${id}/refund`,
    method: "post",
    params: { reason },
  });
}

export function processRefund(id, approved, remark) {
  return request({
    url: `/order/list/${id}/process-refund`,
    method: "post",
    params: { approved, remark },
  });
}

// 创建订单
export function createOrder(userId, cartIds, consignee, phone, address, remarks) {
  return request({
    url: "/order/list/create",
    method: "post",
    data: cartIds,
    params: { userId, consignee, phone, address, remarks },
  });
}

// 根据用户ID获取订单
export function getOrdersByUserId(userId, orderStatus) {
  return request({
    url: `/order/list/user/${userId}`,
    method: "get",
    params: { orderStatus },
  });
}

// 订单统计
export function getOrderStatistics(params) {
  return request({
    url: "/order/statistics",
    method: "get",
    params,
  });
}

export function getOrderTrend(params) {
  return request({
    url: "/order/trend",
    method: "get",
    params,
  });
}
