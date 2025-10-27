package com.linyi.cropseed.trace.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 支付宝支付DTO
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class AlipayDTO {

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
     * 商品标题
     */
    @NotBlank(message = "商品标题不能为空")
    private String subject;

    /**
     * 商品描述
     */
    private String body;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 返回地址
     */
    private String returnUrl;
}
