package com.linyi.cropseed.trace.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.entity.OrderInfo;
import com.linyi.cropseed.trace.vo.OrderDetailVO;
import com.linyi.cropseed.trace.vo.OrderVO;

import java.util.List;

/**
 * 订单服务接口
 * 
 * @author LinYi
 * @since 2025-10-25
 */
public interface OrderService extends IService<OrderInfo> {

    /**
     * 分页查询订单列表
     */
    PageResult<OrderVO> pageOrders(PageQuery pageQuery, Long userId, Integer orderType, Integer orderStatus,
            String orderNo);

    /**
     * 根据用户ID查询订单列表
     */
    List<OrderVO> getOrdersByUserId(Long userId, Integer orderStatus);

    /**
     * 创建订单
     */
    Long createOrder(Long userId, List<Long> cartIds, String consignee, String phone, String address, String remarks);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, String reason);

    /**
     * 支付订单
     */
    void payOrder(Long orderId, Integer paymentMethod);

    /**
     * 审核订单
     */
    void auditOrder(Long orderId, boolean approved, String remark);

    /**
     * 发货
     */
    void shipOrder(Long orderId, String logisticsCompany, String trackingNumber, String remark);

    /**
     * 完成订单
     */
    void completeOrder(Long orderId);

    /**
     * 申请退款
     */
    void refundOrder(Long orderId, String reason);

    /**
     * 处理退款
     */
    void processRefund(Long orderId, boolean approved, String remark);

    /**
     * 获取订单详情
     */
    OrderDetailVO getOrderDetail(Long orderId);

    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long orderId, Integer newStatus, String operation, String remark);

    /**
     * 记录订单操作日志
     */
    void logOrderOperation(Long orderId, String operation, Integer fromStatus, Integer toStatus, String remark);
}
