import http from "@/common/http.js";

/**
 * 获取售后列表
 * @param {Object} params - 查询参数
 * @param {Number} params.status - 售后状态（可选）
 */
export function getAfterSaleList(params) {
  return http.get("/wx/after-sales", params);
}

/**
 * 获取售后详情
 * @param {Number} id - 售后ID
 */
export function getAfterSaleDetail(id) {
  return http.get(`/wx/after-sales/${id}`);
}

/**
 * 申请售后
 * @param {Object} data - 售后数据
 * @param {Number} data.orderId - 订单ID
 * @param {Number} data.type - 售后类型：1-退货，2-换货
 * @param {String} data.reason - 售后原因
 * @param {Array} data.evidenceImages - 凭证图片数组
 */
export function applyAfterSale(data) {
  return http.post("/wx/after-sales", data);
}

/**
 * 取消售后申请
 * @param {Number} id - 售后ID
 */
export function cancelAfterSale(id) {
  return http.post(`/wx/after-sales/${id}/cancel`);
}
