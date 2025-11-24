package com.linyi.cropseed.trace.module.recommendation.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推荐结果VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class RecommendationVO {

    /**
     * 推荐ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 推荐类型：1-协同过滤，2-内容推荐，3-热门推荐，4-个性化推荐
     */
    private Integer recommendationType;

    /**
     * 推荐类型名称
     */
    private String recommendationTypeName;

    /**
     * 目标类型：1-种子，2-品类
     */
    private Integer targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 目标名称
     */
    private String targetName;

    /**
     * 目标图片
     */
    private String targetImage;

    /**
     * 目标价格
     */
    private BigDecimal targetPrice;

    /**
     * 推荐分数：0-1之间的小数
     */
    private BigDecimal recommendationScore;

    /**
     * 推荐理由
     */
    private String recommendationReason;

    /**
     * 推荐权重：0-1之间的小数
     */
    private BigDecimal recommendationWeight;

    /**
     * 是否已展示：0-未展示，1-已展示
     */
    private Integer isShown;

    /**
     * 是否已点击：0-未点击，1-已点击
     */
    private Integer isClicked;

    /**
     * 是否已购买：0-未购买，1-已购买
     */
    private Integer isPurchased;

    /**
     * 点击时间
     */
    private LocalDateTime clickTime;

    /**
     * 购买时间
     */
    private LocalDateTime purchaseTime;

    /**
     * 推荐算法版本
     */
    private String algorithmVersion;

    /**
     * 推荐批次
     */
    private String batchId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
