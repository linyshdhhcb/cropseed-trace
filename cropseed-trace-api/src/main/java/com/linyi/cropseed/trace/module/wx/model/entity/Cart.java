package com.linyi.cropseed.trace.module.wx.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart")
@Schema(description = "购物车")
public class Cart extends BaseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "种子ID")
    private Long seedId;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "是否选中：0-否，1-是")
    private Integer selected;
}
