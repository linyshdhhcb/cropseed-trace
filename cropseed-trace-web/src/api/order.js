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

export function updateOrderStatus(id, status) {
  return request({
    url: `/order/list/${id}/status`,
    method: "put",
    data: { status },
  });
}

export function cancelOrder(id, reason) {
  return request({
    url: `/order/list/${id}/cancel`,
    method: "put",
    data: { reason },
  });
}

export function shipOrder(id, data) {
  return request({
    url: `/order/list/${id}/ship`,
    method: "put",
    data,
  });
}

export function completeOrder(id) {
  return request({
    url: `/order/list/${id}/complete`,
    method: "put",
  });
}

export function deleteOrder(id) {
  return request({
    url: `/order/list/${id}`,
    method: "delete",
  });
}

export function batchDeleteOrder(ids) {
  return request({
    url: "/order/list/batch",
    method: "delete",
    data: { ids },
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
