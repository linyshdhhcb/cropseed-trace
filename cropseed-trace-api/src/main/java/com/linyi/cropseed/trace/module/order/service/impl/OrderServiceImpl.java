package com.linyi.cropseed.trace.module.order.service.impl;

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
import com.linyi.cropseed.trace.module.wx.model.dto.OrderSubmitGoodsDTO;
import com.linyi.cropseed.trace.module.inventory.model.entity.Inventory;
import com.linyi.cropseed.trace.module.order.mapper.OrderInfoMapper;
import com.linyi.cropseed.trace.module.order.mapper.OrderItemMapper;
import com.linyi.cropseed.trace.module.order.mapper.OrderOperationLogMapper;
import com.linyi.cropseed.trace.module.order.model.entity.OrderInfo;
import com.linyi.cropseed.trace.module.order.model.entity.OrderItem;
import com.linyi.cropseed.trace.module.order.model.entity.OrderOperationLog;
import com.linyi.cropseed.trace.module.seed.mapper.SeedInfoMapper;
import com.linyi.cropseed.trace.module.seed.model.entity.SeedInfo;
import com.linyi.cropseed.trace.module.inventory.service.InventoryService;
import com.linyi.cropseed.trace.module.order.service.OrderService;
import com.linyi.cropseed.trace.module.order.model.vo.OrderDetailVO;
import com.linyi.cropseed.trace.module.order.model.vo.OrderItemVO;
import com.linyi.cropseed.trace.module.order.model.vo.OrderVO;
import com.linyi.cropseed.trace.module.wx.mapper.CartMapper;
import com.linyi.cropseed.trace.module.wx.model.entity.Cart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

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
    private final RedissonClient redissonClient;

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

        // 验证购物车商品是否属于当前用户
        for (Cart cartItem : cartItems) {
            if (!cartItem.getUserId().equals(userId)) {
                throw new BusinessException("购物车商品不属于当前用户");
            }
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

        // 检查库存并计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cartItem : cartItems) {
            SeedInfo seedInfo = seedInfoMapper.selectById(cartItem.getSeedId());
            if (seedInfo == null) {
                throw new BusinessException("商品不存在");
            }

            // 检查库存是否充足
            Integer availableStock = inventoryService.getTotalInventoryBySeedId(cartItem.getSeedId());
            if (availableStock == null || availableStock < cartItem.getQuantity()) {
                throw new BusinessException("商品【" + seedInfo.getSeedName() + "】库存不足，当前库存：" +
                    (availableStock != null ? availableStock : 0) + "件，需要：" + cartItem.getQuantity() + "件");
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

        // 删除购物车商品（逻辑删除）
        for (Long cartId : cartIds) {
            Cart cartItem = cartMapper.selectById(cartId);
            if (cartItem != null && cartItem.getUserId().equals(userId)) {
                cartItem.setDeletedFlag(1);
                cartMapper.updateById(cartItem);
            }
        }

        // 记录操作日志
        logOrderOperation(order.getId(), "创建订单", null, OrderConstant.ORDER_STATUS_UNPAID, "用户创建订单");

        log.info("订单创建成功：订单ID={}, 订单号={}", order.getId(), order.getOrderNo());
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrderFromGoods(Long userId, List<OrderSubmitGoodsDTO> goodsItems, String consignee,
            String phone, String address, String remarks) {
        if (CollUtil.isEmpty(goodsItems)) {
            throw new BusinessException("下单商品不能为空");
        }

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

        // 检查库存并计算订单金额
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderSubmitGoodsDTO goodsItem : goodsItems) {
            SeedInfo seedInfo = seedInfoMapper.selectById(goodsItem.getSeedId());
            if (seedInfo == null) {
                throw new BusinessException("商品不存在");
            }

            // 检查库存是否充足
            Integer availableStock = inventoryService.getTotalInventoryBySeedId(goodsItem.getSeedId());
            if (availableStock == null || availableStock < goodsItem.getQuantity()) {
                throw new BusinessException("商品【" + seedInfo.getSeedName() + "】库存不足，当前库存：" +
                    (availableStock != null ? availableStock : 0) + "件，需要：" + goodsItem.getQuantity() + "件");
            }

            // 使用传入的价格，如果没有则使用商品表中的价格
            BigDecimal unitPrice = goodsItem.getUnitPrice() != null
                    && goodsItem.getUnitPrice().compareTo(BigDecimal.ZERO) > 0
                            ? goodsItem.getUnitPrice()
                            : seedInfo.getUnitPrice();
            BigDecimal itemAmount = unitPrice.multiply(new BigDecimal(goodsItem.getQuantity()));
            totalAmount = totalAmount.add(itemAmount);
        }

        order.setTotalAmount(totalAmount);
        order.setPayableAmount(totalAmount);
        this.save(order);

        for (OrderSubmitGoodsDTO goodsItem : goodsItems) {
            SeedInfo seedInfo = seedInfoMapper.selectById(goodsItem.getSeedId());
            if (seedInfo == null) {
                throw new BusinessException("商品不存在");
            }

            // 使用传入的价格，如果没有则使用商品表中的价格
            BigDecimal unitPrice = goodsItem.getUnitPrice() != null
                    && goodsItem.getUnitPrice().compareTo(BigDecimal.ZERO) > 0
                            ? goodsItem.getUnitPrice()
                            : seedInfo.getUnitPrice();

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setSeedId(seedInfo.getId());
            orderItem.setSeedName(seedInfo.getSeedName());
            orderItem.setSeedImage(seedInfo.getImageUrl());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setQuantity(goodsItem.getQuantity());
            orderItem.setTotalAmount(unitPrice.multiply(new BigDecimal(goodsItem.getQuantity())));
            orderItemMapper.insert(orderItem);
        }

        logOrderOperation(order.getId(), "创建订单", null, OrderConstant.ORDER_STATUS_UNPAID, "用户立即购买创建订单");
        log.info("立即购买订单创建成功：订单ID={}, 订单号={}", order.getId(), order.getOrderNo());
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        OrderInfo order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        // 只有未支付、已支付待审核、待发货状态的订单可以取消
        if (!OrderConstant.ORDER_STATUS_UNPAID.equals(order.getOrderStatus()) &&
            !OrderConstant.ORDER_STATUS_PENDING.equals(order.getOrderStatus()) &&
            !OrderConstant.ORDER_STATUS_TO_SHIP.equals(order.getOrderStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 如果订单已支付，需要回滚库存
        if (OrderConstant.ORDER_STATUS_PENDING.equals(order.getOrderStatus()) ||
            OrderConstant.ORDER_STATUS_TO_SHIP.equals(order.getOrderStatus())) {

            // 获取订单商品明细
            LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);

            // 回滚库存
            for (OrderItem item : orderItems) {
                if (item.getBatchId() != null) {
                    try {
                        // 找到对应的仓库（默认使用第一个仓库，实际应该记录出库时的仓库）
                        List<Inventory> inventories = inventoryService.getAvailableInventoryBySeedId(item.getSeedId());
                        if (!inventories.isEmpty()) {
                            Inventory inventory = inventories.get(0); // 简化处理，使用第一个仓库

                            // 回滚库存
                            inventoryService.inbound(
                                item.getSeedId(),
                                item.getBatchId(),
                                inventory.getWarehouseId(),
                                item.getQuantity(),
                                "订单取消回滚库存"
                            );
                        }
                    } catch (Exception e) {
                        log.error("回滚库存失败：订单ID={}, 商品ID={}, 错误信息={}", orderId, item.getSeedId(), e.getMessage());
                        // 库存回滚失败不影响订单取消，只记录日志
                    }
                }
            }
        }

        updateOrderStatus(orderId, OrderConstant.ORDER_STATUS_CANCELLED, "取消订单", reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Integer paymentMethod) {
        // 使用分布式锁防止重复支付和超卖
        String lockKey = "order:pay:" + orderId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待5秒，锁定30秒后自动释放
            boolean acquired = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException("订单正在处理中，请稍后重试");
            }

            OrderInfo order = this.getById(orderId);
            if (order == null) {
                throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
            }

            if (!OrderConstant.ORDER_STATUS_UNPAID.equals(order.getOrderStatus())) {
                throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
            }

            // 获取订单商品明细
            LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);

            // 扣减库存 - 使用先进先出原则选择批次
            for (OrderItem item : orderItems) {
                // 为每个商品使用独立的库存锁，防止并发扣减
                String inventoryLockKey = "inventory:deduct:" + item.getSeedId();
                RLock inventoryLock = redissonClient.getLock(inventoryLockKey);

                try {
                    // 获取库存锁，最多等待3秒
                    boolean inventoryLockAcquired = inventoryLock.tryLock(3, 10, TimeUnit.SECONDS);
                    if (!inventoryLockAcquired) {
                        throw new BusinessException("商品 " + item.getSeedName() + " 库存处理中，请稍后重试");
                    }

                    // 查找该种子的可用库存（按生产日期排序，先进先出）
                    List<Inventory> availableInventories = inventoryService.getAvailableInventoryBySeedId(item.getSeedId());

                    int remainingQuantity = item.getQuantity();
                    for (Inventory inventory : availableInventories) {
                        if (remainingQuantity <= 0) break;

                        int deductQuantity = Math.min(remainingQuantity, inventory.getAvailableQuantity());
                        if (deductQuantity > 0) {
                            // 扣减库存
                            inventoryService.outbound(
                                inventory.getSeedId(),
                                inventory.getBatchId(),
                                inventory.getWarehouseId(),
                                deductQuantity,
                                "订单出库",
                                orderId,
                                "订单支付自动出库"
                            );

                            // 更新订单商品的批次信息
                            item.setBatchId(inventory.getBatchId());
                            orderItemMapper.updateById(item);

                            remainingQuantity -= deductQuantity;
                        }
                    }

                    // 如果库存不足，抛出异常
                    if (remainingQuantity > 0) {
                        throw new BusinessException("商品 " + item.getSeedName() + " 库存不足，缺少 " + remainingQuantity + " 件");
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException("库存扣减处理被中断");
                } catch (Exception e) {
                    log.error("扣减库存失败：订单ID={}, 商品ID={}, 错误信息={}", orderId, item.getSeedId(), e.getMessage());
                    throw new BusinessException("库存扣减失败：" + e.getMessage());
                } finally {
                    // 释放库存锁
                    if (inventoryLock.isHeldByCurrentThread()) {
                        inventoryLock.unlock();
                    }
                }
            }

            // 更新订单状态
            order.setOrderStatus(OrderConstant.ORDER_STATUS_PENDING);
            order.setPaymentMethod(paymentMethod);
            order.setPaymentTime(LocalDateTime.now());
            order.setPaidAmount(order.getPayableAmount());
            this.updateById(order);

            // 记录操作日志
            logOrderOperation(orderId, "支付订单", OrderConstant.ORDER_STATUS_UNPAID, OrderConstant.ORDER_STATUS_PENDING,
                    "订单支付成功，库存已扣减");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("支付处理被中断");
        } catch (Exception e) {
            log.error("支付订单失败：订单ID={}, 错误信息={}", orderId, e.getMessage());
            throw e;
        } finally {
            // 释放分布式锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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
        if (order == null || order.getDeletedFlag() == 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        OrderDetailVO orderDetail = BeanUtil.copyProperties(order, OrderDetailVO.class);

        // 查询订单商品明细
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        List<OrderItemVO> orderItemVOs = orderItems.stream()
                .map(item -> BeanUtil.copyProperties(item, OrderItemVO.class))
                .collect(Collectors.toList());

        orderDetail.setOrderItems(orderItemVOs);

        // 查询订单操作日志
        LambdaQueryWrapper<OrderOperationLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(OrderOperationLog::getOrderId, orderId)
                .orderByDesc(OrderOperationLog::getCreateTime);
        List<OrderOperationLog> operationLogs = orderOperationLogMapper.selectList(logWrapper);
        orderDetail.setOperationLogs(operationLogs);

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

    @Override
    public OrderInfo getOrderStatusById(Long orderId) {
        // 只查询必要的字段，减少数据库查询负担
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getId, orderId)
               .select(OrderInfo::getId, OrderInfo::getOrderNo, OrderInfo::getOrderStatus,
                      OrderInfo::getPaymentMethod, OrderInfo::getPaymentTime);
        return this.getOne(wrapper);
    }

    /**
     * 转换为VO对象
     */
    private OrderVO convertToVO(OrderInfo order) {
        return BeanUtil.copyProperties(order, OrderVO.class);
    }
}
