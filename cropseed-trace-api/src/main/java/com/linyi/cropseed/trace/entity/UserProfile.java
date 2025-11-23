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
 * 用户画像实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_profile")
public class UserProfile extends BaseEntity {

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
     * 用户类型：1-个人用户，2-企业用户
     */
    @TableField("user_type")
    private Integer userType;

    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 地区
     */
    @TableField("region")
    private String region;

    /**
     * 种植经验：1-新手，2-有经验，3-专家
     */
    @TableField("planting_experience")
    private Integer plantingExperience;

    /**
     * 种植规模：1-小规模，2-中规模，3-大规模
     */
    @TableField("planting_scale")
    private Integer plantingScale;

    /**
     * 主要作物类型
     */
    @TableField("main_crops")
    private String mainCrops;

    /**
     * 购买偏好：1-价格敏感，2-质量优先，3-品牌优先
     */
    @TableField("purchase_preference")
    private Integer purchasePreference;

    /**
     * 价格敏感度：0-1之间的小数
     */
    @TableField("price_sensitivity")
    private BigDecimal priceSensitivity;

    /**
     * 质量要求：0-1之间的小数
     */
    @TableField("quality_requirement")
    private BigDecimal qualityRequirement;

    /**
     * 品牌忠诚度：0-1之间的小数
     */
    @TableField("brand_loyalty")
    private BigDecimal brandLoyalty;

    /**
     * 活跃度：0-1之间的小数
     */
    @TableField("activity_level")
    private BigDecimal activityLevel;

    /**
     * 购买频率：0-1之间的小数
     */
    @TableField("purchase_frequency")
    private BigDecimal purchaseFrequency;

    /**
     * 用户标签（JSON格式）
     */
    @TableField("user_tags")
    private String userTags;

    /**
     * 推荐权重：0-1之间的小数
     */
    @TableField("recommendation_weight")
    private BigDecimal recommendationWeight;
}
