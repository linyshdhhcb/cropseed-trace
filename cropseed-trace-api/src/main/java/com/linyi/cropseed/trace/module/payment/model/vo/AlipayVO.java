package com.linyi.cropseed.trace.module.payment.model.vo;

import lombok.Data;

/**
 * 支付宝支付VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class AlipayVO {

    /**
     * 支付URL（网页支付）
     */
    private String payUrl;

    /**
     * 支付表单（手机支付）
     */
    private String payForm;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 交易号
     */
    private String tradeNo;

    /**
     * 支付状态
     */
    private String tradeStatus;

    /**
     * 二维码URL
     */
    private String qrCodeUrl;
}
