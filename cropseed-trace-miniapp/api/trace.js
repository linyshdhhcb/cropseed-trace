import http from "@/common/http.js";

/**
 * 扫码查询溯源信息
 * @param {String} traceCode - 溯源码
 * @param {String} openid - 微信openid（可选）
 */
export function scanTraceCode(traceCode, openid) {
  return http.get(`/wx/trace/scan/${traceCode}`, {
    openid
  });
}

/**
 * 手动输入查询溯源信息
 * @param {String} traceCode - 溯源码
 * @param {String} openid - 微信openid（可选）
 */
export function queryTraceCode(traceCode, openid) {
  return http.get(`/wx/trace/query/${traceCode}`, {
    openid
  });
}

/**
 * 验证溯源码
 * @param {String} traceCode - 溯源码
 */
export function verifyTraceCode(traceCode) {
  return http.get(`/wx/trace/verify/${traceCode}`);
}
