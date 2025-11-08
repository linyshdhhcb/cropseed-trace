package com.linyi.cropseed.trace.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 小程序购物车返回 VO
 */
@Data
public class CartItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "购物车ID")
    private Long cartId;

    @Schema(description = "商品ID")
    private Long seedId;

    @Schema(description = "商品名称")
    private String seedName;

    @Schema(description = "商品图片")
    private String imageUrl;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "是否选中")
    private Boolean selected;

    @Schema(description = "小计金额")
    private BigDecimal totalAmount;
}
