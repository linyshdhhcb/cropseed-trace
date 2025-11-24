package com.linyi.cropseed.trace.module.order.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单商品明细VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "订单商品明细VO")
public class OrderItemVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "种子ID")
    private Long seedId;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "种子名称")
    private String seedName;

    @Schema(description = "种子图片")
    private String seedImage;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;
}
