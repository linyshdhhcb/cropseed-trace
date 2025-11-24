package com.linyi.cropseed.trace.module.recommendation.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品特征实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@TableName("seed_features")
public class SeedFeatures {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 种子ID
     */
    @TableField("seed_id")
    private Long seedId;

    /**
     * 价格特征：0-1之间的小数
     */
    @TableField("price_feature")
    private BigDecimal priceFeature;

    /**
     * 质量特征：0-1之间的小数
     */
    @TableField("quality_feature")
    private BigDecimal qualityFeature;

    /**
     * 品牌特征：0-1之间的小数
     */
    @TableField("brand_feature")
    private BigDecimal brandFeature;

    /**
     * 产量特征：0-1之间的小数
     */
    @TableField("yield_feature")
    private BigDecimal yieldFeature;

    /**
     * 抗病性特征：0-1之间的小数
     */
    @TableField("disease_resistance_feature")
    private BigDecimal diseaseResistanceFeature;

    /**
     * 适应性特征：0-1之间的小数
     */
    @TableField("adaptability_feature")
    private BigDecimal adaptabilityFeature;

    /**
     * 成熟期特征：0-1之间的小数
     */
    @TableField("maturity_feature")
    private BigDecimal maturityFeature;

    /**
     * 种植难度特征：0-1之间的小数
     */
    @TableField("planting_difficulty_feature")
    private BigDecimal plantingDifficultyFeature;

    /**
     * 市场热度：0-1之间的小数
     */
    @TableField("market_heat")
    private BigDecimal marketHeat;

    /**
     * 用户评分：0-1之间的小数
     */
    @TableField("user_rating")
    private BigDecimal userRating;

    /**
     * 销量特征：0-1之间的小数
     */
    @TableField("sales_feature")
    private BigDecimal salesFeature;

    /**
     * 库存特征：0-1之间的小数
     */
    @TableField("inventory_feature")
    private BigDecimal inventoryFeature;

    /**
     * 推荐权重：0-1之间的小数
     */
    @TableField("recommendation_weight")
    private BigDecimal recommendationWeight;

    /**
     * 特征向量（JSON格式）
     */
    @TableField("feature_vector")
    private String featureVector;


    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "创建用户")
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @Schema(description = "修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "修改用户")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @Schema(description = "删除标记：0-未删除，1-已删除")
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deletedFlag;
}
