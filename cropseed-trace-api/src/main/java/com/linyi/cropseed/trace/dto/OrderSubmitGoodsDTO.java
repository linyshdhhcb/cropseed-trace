package com.linyi.cropseed.trace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 小程序下单商品明细 DTO
 *
 * @author LinYi
 */
@Data
public class OrderSubmitGoodsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    @NotNull(message = "商品ID不能为空")
    private Long seedId;

    @Schema(description = "购买数量")
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1")
    private Integer quantity;
}
