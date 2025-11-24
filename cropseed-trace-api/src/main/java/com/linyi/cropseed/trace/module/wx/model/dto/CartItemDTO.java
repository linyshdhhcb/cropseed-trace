package com.linyi.cropseed.trace.module.wx.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 购物车操作 DTO
 */
@Data
public class CartItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "购物车ID，用于更新时")
    private Long cartId;

    @Schema(description = "商品ID")
    @NotNull(message = "商品ID不能为空")
    private Long seedId;

    @Schema(description = "数量")
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量至少为1")
    private Integer quantity;

    @Schema(description = "是否选中")
    private Boolean selected;
}
