package com.linyi.cropseed.trace.module.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linyi.cropseed.trace.common.constant.OrderConstant;
import com.linyi.cropseed.trace.module.order.mapper.OrderInfoMapper;
import com.linyi.cropseed.trace.module.order.mapper.OrderOperationLogMapper;
import com.linyi.cropseed.trace.module.order.model.entity.OrderInfo;
import com.linyi.cropseed.trace.module.order.model.entity.OrderOperationLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动取消定时任务
 * 
 * @author LinYi
 * @since 2025-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderOperationLogMapper orderOperationLogMapper;

    /**
     * 每分钟执行一次，检查并取消超时未支付的订单
     */
    @Scheduled(fixedRate = 60000) // 每60秒执行一次
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        log.debug("开始执行订单超时取消任务...");
        
        // 计算超时时间点（30分钟前）
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(OrderConstant.ORDER_TIMEOUT_MINUTES);
        
        // 查询所有超时未支付的订单
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOrderStatus, OrderConstant.ORDER_STATUS_UNPAID)
               .eq(OrderInfo::getDeletedFlag, 0)
               .lt(OrderInfo::getCreateTime, timeoutTime);
        
        List<OrderInfo> timeoutOrders = orderInfoMapper.selectList(wrapper);
        
        if (timeoutOrders.isEmpty()) {
            log.debug("没有超时订单需要取消");
            return;
        }
        
        log.info("发现 {} 个超时订单需要取消", timeoutOrders.size());
        
        for (OrderInfo order : timeoutOrders) {
            try {
                cancelTimeoutOrder(order);
                log.info("订单超时自动取消成功：订单ID={}, 订单号={}", order.getId(), order.getOrderNo());
            } catch (Exception e) {
                log.error("订单超时取消失败：订单ID={}, 订单号={}, 错误信息={}", 
                         order.getId(), order.getOrderNo(), e.getMessage());
            }
        }
    }

    /**
     * 取消单个超时订单
     */
    private void cancelTimeoutOrder(OrderInfo order) {
        // 更新订单状态为已取消
        order.setOrderStatus(OrderConstant.ORDER_STATUS_CANCELLED);
        orderInfoMapper.updateById(order);
        
        // 记录操作日志
        OrderOperationLog operationLog = new OrderOperationLog();
        operationLog.setOrderId(order.getId());
        operationLog.setOperatorId(0L); // 系统自动操作，使用0表示系统
        operationLog.setOperatorName("系统");
        operationLog.setOperation("订单超时取消");
        operationLog.setFromStatus(OrderConstant.ORDER_STATUS_UNPAID);
        operationLog.setToStatus(OrderConstant.ORDER_STATUS_CANCELLED);
        operationLog.setRemark("订单超过30分钟未支付，系统自动取消");
        orderOperationLogMapper.insert(operationLog);
    }
}
