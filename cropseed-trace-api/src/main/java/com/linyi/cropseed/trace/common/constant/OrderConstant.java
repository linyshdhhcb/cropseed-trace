package com.linyi.cropseed.trace.common.constant;

/**
 * 订单相关常量
 * 
 * @author LinYi
 * @since 2025-10-25
 */
public interface OrderConstant {

    /**
     * 订单类型 - C端订单
     */
    Integer ORDER_TYPE_C = 1;

    /**
     * 订单类型 - B端订单
     */
    Integer ORDER_TYPE_B = 2;

    /**
     * 订单状态 - 待付款
     */
    Integer ORDER_STATUS_UNPAID = 0;

    /**
     * 订单状态 - 待审核
     */
    Integer ORDER_STATUS_PENDING = 1;

    /**
     * 订单状态 - 待发货
     */
    Integer ORDER_STATUS_TO_SHIP = 2;

    /**
     * 订单状态 - 已发货
     */
    Integer ORDER_STATUS_SHIPPED = 3;

    /**
     * 订单状态 - 已完成
     */
    Integer ORDER_STATUS_COMPLETED = 4;

    /**
     * 订单状态 - 已取消
     */
    Integer ORDER_STATUS_CANCELLED = 5;

    /**
     * 订单状态 - 退款中
     */
    Integer ORDER_STATUS_REFUNDING = 6;

    /**
     * 支付方式 - 微信支付
     */
    Integer PAYMENT_METHOD_WECHAT = 1;

    /**
     * 支付方式 - 支付宝
     */
    Integer PAYMENT_METHOD_ALIPAY = 2;

    /**
     * 订单号前缀
     */
    String ORDER_NO_PREFIX = "ORD";

    /**
     * 订单超时时间（分钟）
     */
    Integer ORDER_TIMEOUT_MINUTES = 30;
}
