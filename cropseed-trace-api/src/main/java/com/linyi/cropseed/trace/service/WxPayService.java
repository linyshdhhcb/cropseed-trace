package com.linyi.cropseed.trace.service;

import com.linyi.cropseed.trace.dto.WxPayDTO;
import com.linyi.cropseed.trace.vo.WxPayVO;

import java.util.Map;

/**
 * 微信支付服务接口
 * 
 * @author LinYi
 * @since 2025-10-25
 */
public interface WxPayService {

    /**
     * 创建微信支付订单
     */
    WxPayVO createPayOrder(WxPayDTO payDTO);

    /**
     * 处理微信支付回调
     */
    String handlePayNotify(Map<String, String> notifyData);

    /**
     * 查询支付结果
     */
    boolean queryPayResult(String orderNo);

    /**
     * 申请退款
     */
    boolean refund(String orderNo, String refundNo, Integer refundAmount, String reason);
}
