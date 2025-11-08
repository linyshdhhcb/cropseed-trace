import http from "@/common/http.js";

/**
 * 获取购物车列表
 */
export function getCartList() {
  return http.get("/wx/cart");
}

/**
 * 添加商品到购物车
 * @param {Object} data - 购物车数据
 * @param {Number} data.seedId - 种子ID
 * @param {Number} data.quantity - 数量
 */
export function addToCart(data) {
  return http.post("/wx/cart", data);
}

/**
 * 更新购物车商品数量
 * @param {Number} id - 购物车ID
 * @param {Number} quantity - 数量
 */
export function updateCartQuantity(id, quantity) {
  return http.put(`/wx/cart/${id}`, { quantity });
}

/**
 * 更新购物车商品选中状态
 * @param {Number} id - 购物车ID
 * @param {Boolean} selected - 是否选中
 */
export function updateCartSelected(id, selected) {
  return http.put(`/wx/cart/${id}`, { selected });
}

/**
 * 批量更新购物车选中状态
 * @param {Array} ids - 购物车ID数组
 * @param {Boolean} selected - 是否选中
 */
export function batchUpdateCartSelected(ids, selected) {
  return http.put("/wx/cart/batch-select", { ids, selected });
}

/**
 * 删除购物车商品
 * @param {Number} id - 购物车ID
 */
export function deleteCart(id) {
  return http.delete(`/wx/cart/${id}`);
}

/**
 * 批量删除购物车商品
 * @param {Array} ids - 购物车ID数组
 */
export function batchDeleteCart(ids) {
  return http.delete("/wx/cart/batch", { ids });
}

/**
 * 清空购物车
 */
export function clearCart() {
  return http.post("/wx/cart/clear");
}
