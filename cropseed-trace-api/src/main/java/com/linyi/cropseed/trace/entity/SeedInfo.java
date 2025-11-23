package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 种子信息实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seed_info")
@Schema(description = "种子信息")
public class SeedInfo extends BaseEntity {

    @Schema(description = "种子名称")
    private String seedName;

    @Schema(description = "种子编码")
    private String seedCode;

    @Schema(description = "品类ID")
    private Long categoryId;

    @Schema(description = "品种")
    private String variety;

    @Schema(description = "产地")
    private String originPlace;

    @Schema(description = "特性描述")
    private String characteristics;

    @Schema(description = "规格参数")
    private String specifications;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "状态：0-下架，1-上架")
    private Integer status;
}
