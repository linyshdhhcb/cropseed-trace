package com.linyi.cropseed.trace.module.recommendation.service;

import com.linyi.cropseed.trace.module.recommendation.model.vo.RecommendationVO;

import java.util.List;

/**
 * 推荐系统服务接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface RecommendationService {

    /**
     * 协同过滤推荐
     */
    List<RecommendationVO> collaborativeFilteringRecommend(Long userId, Integer limit);

    /**
     * 内容推荐
     */
    List<RecommendationVO> contentBasedRecommend(Long userId, Integer limit);

    /**
     * 热门推荐
     */
    List<RecommendationVO> popularRecommend(Integer limit);

    /**
     * 个性化推荐
     */
    List<RecommendationVO> personalizedRecommend(Long userId, Integer limit);

    /**
     * 混合推荐
     */
    List<RecommendationVO> hybridRecommend(Long userId, Integer limit);

    /**
     * 记录用户行为
     */
    void recordUserBehavior(Long userId, Integer behaviorType, Integer targetType, Long targetId,
            String behaviorContent, Double behaviorWeight);

    /**
     * 更新用户画像
     */
    void updateUserProfile(Long userId);

    /**
     * 计算商品特征
     */
    void calculateSeedFeatures(Long seedId);

    /**
     * 批量生成推荐结果
     */
    void generateRecommendations(Long userId);

    /**
     * 记录推荐点击
     */
    void recordRecommendationClick(Long recommendationId);

    /**
     * 记录推荐购买
     */
    void recordRecommendationPurchase(Long recommendationId);
}
