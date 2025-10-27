package com.linyi.cropseed.trace.vo;

import lombok.Data;

/**
 * 微信支付VO
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class WxPayVO {

    /**
     * 预支付交易会话标识
     */
    private String prepayId;

    /**
     * 时间戳
     */
    private String timeStamp;

    /**
     * 随机字符串
     */
    private String nonceStr;

    /**
     * 签名
     */
    private String paySign;

    /**
     * 签名类型
     */
    private String signType;

    /**
     * 小程序调起支付参数
     */
    private String packageValue;
}
