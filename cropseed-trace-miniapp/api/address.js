import http from "@/common/http.js";

/**
 * 获取收货地址列表
 */
export function getAddressList() {
  return http.get("/wx/address");
}

/**
 * 获取收货地址详情
 * @param {Number} id - 地址ID
 */
export function getAddressDetail(id) {
  return http.get(`/wx/address/${id}`);
}

/**
 * 新增收货地址
 * @param {Object} data - 地址数据
 * @param {String} data.consignee - 收货人
 * @param {String} data.phone - 联系电话
 * @param {String} data.province - 省份
 * @param {String} data.city - 城市
 * @param {String} data.district - 区县
 * @param {String} data.detailAddress - 详细地址
 * @param {Boolean} data.isDefault - 是否默认
 */
export function addAddress(data) {
  return http.post("/wx/address", data);
}

/**
 * 修改收货地址
 * @param {Number} id - 地址ID
 * @param {Object} data - 地址数据
 */
export function updateAddress(id, data) {
  return http.put(`/wx/address/${id}`, data);
}

/**
 * 删除收货地址
 * @param {Number} id - 地址ID
 */
export function deleteAddress(id) {
  return http.delete(`/wx/address/${id}`);
}

/**
 * 设置默认地址
 * @param {Number} id - 地址ID
 */
export function setDefaultAddress(id) {
  return http.put(`/wx/address/${id}/default`);
}

/**
 * 获取默认地址
 */
export function getDefaultAddress() {
  return http.get("/wx/address/default");
}
