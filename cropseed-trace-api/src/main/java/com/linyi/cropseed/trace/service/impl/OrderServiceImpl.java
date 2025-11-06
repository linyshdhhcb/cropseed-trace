package com.linyi.cropseed.trace.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.constant.OrderConstant;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.IdGenerator;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import com.linyi.cropseed.trace.entity.*;
import com.linyi.cropseed.trace.mapper.*;
import com.linyi.cropseed.trace.service.InventoryService;
import com.linyi.cropseed.trace.service.OrderService;
import com.linyi.cropseed.trace.vo.OrderDetailVO;
import com.linyi.cropseed.trace.vo.OrderItemVO;
import com.linyi.cropseed.trace.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final OrderOperationLogMapper orderOperationLogMapper;
    private final CartMapper cartMapper;
    private final SeedInfoMapper seedInfoMapper;
    private final InventoryService inventoryService;

    @Override
    public PageResult<OrderVO> pageOrders(PageQuery pageQuery, Long userId, Integer orderType, Integer orderStatus,
            String orderNo) {
        Page<OrderInfo> page = pageQuery.toMpPageWithOrder();

        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, OrderInfo::getUserId, userId)
                .eq(orderType != null, OrderInfo::getOrderType, orderType)
                .eq(orderStatus != null, OrderInfo::getOrderStatus, orderStatus)
                .like(orderNo != null, OrderInfo::getOrderNo, orderNo)
                .orderByDesc(OrderInfo::getCreateTime);

        Page<OrderInfo> orderPage = this.page(page, wrapper);

        List<OrderVO> orderVOList = orderPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(orderPage, orderVOList);
    }

    @Override
    public List<OrderVO> getOrdersByUserId(Long userId, Integer orderStatus) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getUserId, userId);
        if (orderStatus != null) {
            wrapper.eq(OrderInfo::getOrderStatus, orderStatus);
        }
        wrapper.orderByDesc(OrderInfo::getCreateTime);

        List<OrderInfo> orders = this.list(wrapper);
        return orders.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long userId, List<Long> cartIds, String consignee, String phone, String address,
            String remarks) {
        if (CollUtil.isEmpty(cartIds)) {
            throw new BusinessException("购物车不能为空");
        }

        // 查询购物车商品
        List<Cart> cartItems = cartMapper.selectBatchIds(cartIds);
        if (CollUtil.isEmpty(cartItems)) {
            throw new BusinessException("购物车商品不存在");
        }

        // 创建订单
        OrderInfo order = new OrderInfo();
        order.setOrderNo(IdGenerator.generateOrderNo());
        order.setUserId(userId);
        order.setOrderType(OrderConstant.ORDER_TYPE_C);
        order.setOrderStatus(OrderConstant.ORDER_STATUS_UNPAID);
        order.setConsignee(consignee);
        order.setPhone(phone);
        order.setAddress(address);
        order.setRemarks(remarks);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setPaidAmount(BigDecimal.ZERO);

        // 计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cartItem : cartItems) {
            SeedInfo seedInfo = seedInfoMapper.selectById(cartItem.getSeedId());
            if (seedInfo == null) {
                throw new BusinessException("商品不存在");
            }
            totalAmount = totalAmount.add(seedInfo.getUnitPrice().multiply(new BigDecimal(cartItem.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        order.setPayableAmount(totalAmount);
        this.save(order);

        // 创建订单商品明细
        for (Cart cartItem : cartItems) {
            SeedInfo seedInfo = seedInfoMapper.selectById(cartItem.getSeedId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setSeedId(cartItem.getSeedId());
            orderItem.setSeedName(seedInfo.getSeedName());
            orderItem.setSeedImage(seedInfo.getImageUrl());
            orderItem.setUnitPrice(seedInfo.getUnitPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalAmount(seedInfo.getUnitPrice().multiply(new BigDecimal(cartItem.getQuantity())));
            orderItemMapper.insert(orderItem);
        }

        // 删除购物车商品
        cartMapper.deleteBatchIds(cartIds);

        // 记录操作日志
        logOrderOperation(order.getId(), "创建订单", null, OrderConstant.ORDER_STATUS_UNPAID, "用户创建订单");

        log.info("订单创建成功：订单ID={}, 订单号={}", order.getId(), order.getOrderNo());
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        OrderInfo order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!OrderConstant.ORDER_STATUS_UNPAID.equals(order.getOrderStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        updateOrderStatus(orderId, OrderConstant.ORDER_STATUS_CANCELLED, "取消订单", reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Integer paymentMethod) {
        OrderInfo order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!OrderConstant.ORDER_STATUS_UNPAID.equals(order.getOrderStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 更新订单状态
        order.setOrderStatus(OrderConstant.ORDER_STATUS_PENDING);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTime(LocalDateTime.now());
        order.setPaidAmount(order.getPayableAmount());
        this.updateById(order);

        // 记录操作日志
        logOrderOperation(orderId, "支付订单", OrderConstant.ORDER_STATUS_UNPAID, OrderConstant.ORDER_STATUS_PENDING,
                "订单支付成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditOrder(Long orderId, boolean approved, String remark) {
        OrderInfo order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!OrderConstant.ORDER_STATUS_PENDING.equals(order.getOrderStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        Integer newStatus = approved ? OrderConstant.ORDER_STATUS_TO_SHIP : OrderConstant.ORDER_STATUS_CANCELLED;
        String operation = approved ? "审核通过" : "审核拒绝";

        updateOrderStatus(orderId, newStatus, operation, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, String logisticsCompany, String trackingNumber, String remark) {
        OrderInfo order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!OrderConstant.ORDER_STATUS_TO_SHIP.equals(order.getOrderStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        updateOrderStatus(orderId, OrderConstant.ORDER_STATUS_SHIPPED, "订单发货", remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId) {
        OrderInfo order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!OrderConstant.ORDER_STATUS_SHIPPED.equals(order.getOrderStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        updateOrderStatus(orderId, OrderConstant.ORDER_STATUS_COMPLETED, "订单完成", "订单已完成");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long orderId, String reason) {
        OrderInfo order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!OrderConstant.ORDER_STATUS_SHIPPED.equals(order.getOrderStatus()) &&
                !OrderConstant.ORDER_STATUS_COMPLETED.equals(order.getOrderStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        updateOrderStatus(orderId, OrderConstant.ORDER_STATUS_REFUNDING, "申请退款", reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRefund(Long orderId, boolean approved, String remark) {
        OrderInfo order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!OrderConstant.ORDER_STATUS_REFUNDING.equals(order.getOrderStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        Integer newStatus = approved ? OrderConstant.ORDER_STATUS_CANCELLED : OrderConstant.ORDER_STATUS_SHIPPED;
        String operation = approved ? "退款成功" : "退款拒绝";

        updateOrderStatus(orderId, newStatus, operation, remark);
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        OrderInfo order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        OrderDetailVO orderDetail = BeanUtil.copyProperties(order, OrderDetailVO.class);

        // 查询订单商品明细
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);

        List<OrderItemVO> orderItemVOs = orderItems.stream()
                .map(item -> BeanUtil.copyProperties(item, OrderItemVO.class))
                .collect(Collectors.toList());

        orderDetail.setOrderItems(orderItemVOs);

        return orderDetail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long orderId, Integer newStatus, String operation, String remark) {
        OrderInfo order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        Integer oldStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);
        this.updateById(order);

        // 记录操作日志
        logOrderOperation(orderId, operation, oldStatus, newStatus, remark);
    }

    @Override
    public void logOrderOperation(Long orderId, String operation, Integer fromStatus, Integer toStatus, String remark) {
        OrderOperationLog log = new OrderOperationLog();
        log.setOrderId(orderId);
        log.setOperatorId(SecurityUtils.getCurrentUserId());
        log.setOperatorName(SecurityUtils.getCurrentUsername());
        log.setOperation(operation);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setRemark(remark);
        orderOperationLogMapper.insert(log);
    }

    /**
     * 转换为VO对象
     */
    private OrderVO convertToVO(OrderInfo order) {
        return BeanUtil.copyProperties(order, OrderVO.class);
    }
}
