package com.linyi.cropseed.trace.module.payment.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 微信支付DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class WxPayDTO {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;

    /**
     * 商品描述
     */
    @NotBlank(message = "商品描述不能为空")
    private String description;

    /**
     * 用户openid
     */
    @NotBlank(message = "用户openid不能为空")
    private String openid;

    /**
     * 回调地址
     */
    private String notifyUrl;
}
