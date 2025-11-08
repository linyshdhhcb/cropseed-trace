import http from "@/common/http.js";

/**
 * 创建订单
 * @param {Object} data - 订单数据
 * @param {Array} data.cartIds - 购物车ID列表
 * @param {Number} data.addressId - 收货地址ID
 * @param {String} data.remarks - 订单备注（可选）
 */
export function createOrder(data) {
  return http.post("/wx/order/submit", data);
}

/**
 * 获取订单列表
 * @param {Object} params - 查询参数
 * @param {Number} params.page - 页码
 * @param {Number} params.size - 每页大小
 * @param {Number} params.orderStatus - 订单状态（可选）
 */
export function getOrderList(params) {
  return http.get("/wx/order/page", params);
}

/**
 * 获取订单详情
 * @param {Number} orderId - 订单ID
 */
export function getOrderDetail(orderId) {
  return http.get(`/wx/order/${orderId}`);
}

/**
 * 取消订单
 * @param {Number} orderId - 订单ID
 * @param {String} reason - 取消原因
 */
export function cancelOrder(orderId, reason) {
  return http.post(`/wx/order/${orderId}/cancel`, { reason });
}

/**
 * 支付订单
 * @param {Number} orderId - 订单ID
 * @param {Number} paymentMethod - 支付方式：1-微信支付，2-支付宝
 */
export function payOrder(orderId, paymentMethod = 1) {
  return http.post(`/wx/order/${orderId}/pay`, { paymentMethod });
}

/**
 * 确认收货
 * @param {Number} orderId - 订单ID
 */
export function confirmReceipt(orderId) {
  return http.post(`/wx/order/${orderId}/confirm`);
}

/**
 * 申请退款
 * @param {Number} orderId - 订单ID
 * @param {String} reason - 退款原因
 */
export function applyRefund(orderId, reason) {
  return http.post(`/wx/order/${orderId}/refund`, { reason });
}

/**
 * 删除订单
 * @param {Number} orderId - 订单ID
 */
export function deleteOrder(orderId) {
  return http.delete(`/wx/order/${orderId}`);
}
