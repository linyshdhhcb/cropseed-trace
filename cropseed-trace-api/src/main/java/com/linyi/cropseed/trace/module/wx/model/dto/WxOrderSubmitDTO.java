package com.linyi.cropseed.trace.module.wx.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 小程序订单提交 DTO
 */
@Data
public class WxOrderSubmitDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "购物车ID集合")
    private List<Long> cartIds;

    @Schema(description = "立即购买的商品明细")
    @Valid
    private List<OrderSubmitGoodsDTO> goods;

    @Schema(description = "收货地址ID")
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @Schema(description = "订单备注")
    private String remarks;
}
