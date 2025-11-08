package com.linyi.cropseed.trace.service;

import com.linyi.cropseed.trace.dto.AlipayDTO;
import com.linyi.cropseed.trace.vo.AlipayVO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付服务接口
 * 
 * @author LinYi
 * @since 2025-10-25
 */
public interface AlipayService {

    /**
     * 创建支付订单（网页支付）
     */
    AlipayVO createPayOrder(AlipayDTO payDTO);

    /**
     * 创建支付订单（手机支付）
     */
    AlipayVO createPayOrderMobile(AlipayDTO payDTO);

    /**
     * 处理支付宝回调
     */
    String handlePayNotify(Map<String, String> notifyData);

    /**
     * 查询支付结果
     */
    boolean queryPayResult(String orderNo);

    /**
     * 申请退款
     */
    boolean refund(String orderNo, String refundNo, BigDecimal refundAmount, String reason);

    /**
     * 关闭订单
     */
    boolean closeOrder(String orderNo);
}
