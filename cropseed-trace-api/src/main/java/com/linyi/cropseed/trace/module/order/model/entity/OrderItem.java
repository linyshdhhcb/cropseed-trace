package com.linyi.cropseed.trace.module.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单商品明细实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
@Schema(description = "订单商品明细")
public class OrderItem extends BaseEntity {

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
