package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.linyi.cropseed.trace.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单信息实体
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_info")
@Schema(description = "订单信息")
public class OrderInfo extends BaseEntity {

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "订单类型：1-C端订单，2-B端订单")
    private Integer orderType;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    @Schema(description = "运费")
    private BigDecimal freightAmount;

    @Schema(description = "应付金额")
    private BigDecimal payableAmount;

    @Schema(description = "实付金额")
    private BigDecimal paidAmount;

    @Schema(description = "支付方式：1-微信支付，2-支付宝")
    private Integer paymentMethod;

    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    @Schema(description = "订单状态：0-待付款，1-待审核，2-待发货，3-已发货，4-已完成，5-已取消，6-退款中")
    private Integer orderStatus;

    @Schema(description = "收货人")
    private String consignee;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "收货地址")
    private String address;

    @Schema(description = "订单备注")
    private String remarks;
}
