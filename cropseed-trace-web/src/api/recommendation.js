import request from "./request";

// 推荐结果相关接口

// 协同过滤推荐
export function collaborativeFilteringRecommend(userId, limit = 10) {
  return request({
    url: `/recommendation/collaborative/${userId}`,
    method: "get",
    params: { limit },
  });
}

// 内容推荐
export function contentBasedRecommend(userId, limit = 10) {
  return request({
    url: `/recommendation/content/${userId}`,
    method: "get",
    params: { limit },
  });
}

// 热门推荐
export function popularRecommend(limit = 10) {
  return request({
    url: "/recommendation/popular",
    method: "get",
    params: { limit },
  });
}

// 个性化推荐
export function personalizedRecommend(userId, limit = 10) {
  return request({
    url: `/recommendation/personalized/${userId}`,
    method: "get",
    params: { limit },
  });
}

// 混合推荐
export function hybridRecommend(userId, limit = 20) {
  return request({
    url: `/recommendation/hybrid/${userId}`,
    method: "get",
    params: { limit },
  });
}

// 获取推荐结果列表
export function getRecommendationList(params) {
  return request({
    url: "/recommendation/page",
    method: "get",
    params,
  });
}

// 生成推荐结果
export function generateRecommendations(userId) {
  return request({
    url: `/recommendation/generate/${userId}`,
    method: "post",
  });
}

// 记录推荐点击
export function recordRecommendationClick(recommendationId) {
  return request({
    url: `/recommendation/click/${recommendationId}`,
    method: "post",
  });
}

// 记录推荐购买
export function recordRecommendationPurchase(recommendationId) {
  return request({
    url: `/recommendation/purchase/${recommendationId}`,
    method: "post",
  });
}

// 用户行为相关接口

// 记录用户行为
export function recordUserBehavior(data) {
  return request({
    url: "/recommendation/behavior",
    method: "post",
    params: data,
  });
}

// 获取用户行为列表
export function getUserBehaviorList(params) {
  return request({
    url: "/recommendation/behavior/page",
    method: "get",
    params,
  });
}

// 用户画像相关接口

// 获取用户画像列表
export function getUserProfileList(params) {
  return request({
    url: "/recommendation/profile/page",
    method: "get",
    params,
  });
}

// 获取用户画像详情
export function getUserProfileDetail(userId) {
  return request({
    url: `/recommendation/profile/${userId}`,
    method: "get",
  });
}

// 更新用户画像
export function updateUserProfile(userId) {
  return request({
    url: `/recommendation/profile/update/${userId}`,
    method: "post",
  });
}

// 计算商品特征
export function calculateSeedFeatures(seedId) {
  return request({
    url: `/recommendation/features/calculate/${seedId}`,
    method: "post",
  });
}
