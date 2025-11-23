package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 推荐结果实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("recommendation")
public class Recommendation extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 推荐类型：1-协同过滤，2-内容推荐，3-热门推荐，4-个性化推荐
     */
    @TableField("recommendation_type")
    private Integer recommendationType;

    /**
     * 目标类型：1-种子，2-品类
     */
    @TableField("target_type")
    private Integer targetType;

    /**
     * 目标ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 推荐分数：0-1之间的小数
     */
    @TableField("recommendation_score")
    private BigDecimal recommendationScore;

    /**
     * 推荐理由
     */
    @TableField("recommendation_reason")
    private String recommendationReason;

    /**
     * 推荐权重：0-1之间的小数
     */
    @TableField("recommendation_weight")
    private BigDecimal recommendationWeight;

    /**
     * 是否已展示：0-未展示，1-已展示
     */
    @TableField("is_shown")
    private Integer isShown;

    /**
     * 是否已点击：0-未点击，1-已点击
     */
    @TableField("is_clicked")
    private Integer isClicked;

    /**
     * 是否已购买：0-未购买，1-已购买
     */
    @TableField("is_purchased")
    private Integer isPurchased;

    /**
     * 点击时间
     */
    @TableField("click_time")
    private java.time.LocalDateTime clickTime;

    /**
     * 购买时间
     */
    @TableField("purchase_time")
    private java.time.LocalDateTime purchaseTime;

    /**
     * 推荐算法版本
     */
    @TableField("algorithm_version")
    private String algorithmVersion;

    /**
     * 推荐批次
     */
    @TableField("batch_id")
    private String batchId;
}
