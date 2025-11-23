package com.linyi.cropseed.trace.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 推荐系统配置类
 *
 * @author linyi
 * @since 2025-11-17
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationProperties {

    /**
     * 相似度阈值
     */
    private Double similarityThreshold = 0.1;

    /**
     * 时间衰减天数
     */
    private Integer timeDecayDays = 30;

    /**
     * 最近购买天数
     */
    private Integer recentPurchaseDays = 30;

    /**
     * 行为权重配置
     */
    private BehaviorWeight behaviorWeight = new BehaviorWeight();

    /**
     * 混合推荐权重配置
     */
    private HybridWeight hybridWeight = new HybridWeight();

    /**
     * 行为权重配置类
     */
    @Data
    public static class BehaviorWeight {
        /**
         * 浏览权重
         */
        private Double view = 1.0;

        /**
         * 搜索权重
         */
        private Double search = 1.5;

        /**
         * 收藏权重
         */
        private Double collect = 3.0;

        /**
         * 加购物车权重
         */
        private Double cart = 3.5;

        /**
         * 购买权重
         */
        private Double purchase = 5.0;

        /**
         * 评价权重
         */
        private Double rate = 4.0;

        /**
         * 根据行为类型获取权重
         */
        public Double getWeightByType(Integer behaviorType) {
            switch (behaviorType) {
                case 1: return view;
                case 2: return search;
                case 3: return collect;
                case 4: return cart;
                case 5: return purchase;
                case 6: return rate;
                default: return 1.0;
            }
        }
    }

    /**
     * 混合推荐权重配置类
     */
    @Data
    public static class HybridWeight {
        /**
         * 协同过滤权重
         */
        private Double collaborative = 0.30;

        /**
         * 内容推荐权重
         */
        private Double content = 0.25;

        /**
         * 个性化推荐权重
         */
        private Double personalized = 0.25;

        /**
         * 热门推荐权重
         */
        private Double popular = 0.20;
    }

    /**
     * 用户相似度权重配置类
     */
    @Data
    public static class SimilarityWeight {
        /**
         * 行为相似度权重
         */
        private Double behavior = 0.7;

        /**
         * 画像相似度权重
         */
        private Double profile = 0.3;
    }

    /**
     * 用户相似度权重配置
     */
    private SimilarityWeight similarityWeight = new SimilarityWeight();

    /**
     * 降权配置
     */
    @Data
    public static class DowngradeConfig {
        /**
         * 最近购买商品降权比例
         */
        private Double recentPurchaseRatio = 0.3;

        /**
         * 是否启用降权
         */
        private Boolean enabled = true;
    }

    /**
     * 降权配置
     */
    private DowngradeConfig downgrade = new DowngradeConfig();
}
