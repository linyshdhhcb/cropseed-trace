import http from "@/common/http.js";

/**
 * 获取种子品类树
 */
export function getCategoryTree() {
  return http.get("/wx/product/category/tree");
}

/**
 * 获取商品列表（分页）
 * @param {Object} params - 查询参数
 * @param {Number} params.page - 页码
 * @param {Number} params.size - 每页大小
 * @param {String} params.seedName - 种子名称（可选）
 * @param {Number} params.categoryId - 品类ID（可选）
 * @param {String} params.sortField - 排序字段（可选）
 * @param {String} params.sortOrder - 排序方式（可选：asc/desc）
 */
export function getProductList(params) {
  return http.get("/wx/product/page", params);
}

/**
 * 获取商品详情
 * @param {Number} id - 商品ID
 */
export function getProductDetail(id) {
  return http.get(`/wx/product/${id}`);
}

/**
 * 搜索商品
 * @param {String} keyword - 搜索关键词
 * @param {Number} page - 页码
 * @param {Number} size - 每页大小
 */
export function searchProduct(keyword, page = 1, size = 10) {
  return http.get("/wx/product/page", {
    seedName: keyword,
    page,
    size,
  });
}

/**
 * 获取推荐商品
 */
export function getRecommendProducts() {
  return http.get("/wx/recommend/list");
}

/**
 * 上报用户行为
 * @param {Object} data - 行为数据
 * @param {Number} data.seedId - 种子ID
 * @param {Number} data.behaviorType - 行为类型：1-浏览，2-点击，3-加入购物车，4-购买，5-收藏
 * @param {Number} data.duration - 停留时长（秒）
 * @param {String} data.source - 行为来源
 */
export function reportBehavior(data) {
  return http.post("/wx/recommend/behavior", data);
}
