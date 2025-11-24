package com.linyi.cropseed.trace.module.order.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.order.service.OrderService;
import com.linyi.cropseed.trace.module.order.model.vo.OrderDetailVO;
import com.linyi.cropseed.trace.module.order.model.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单管理Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/order/list")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "分页查询订单列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('order:list')")
    public Result<PageResult<OrderVO>> pageOrders(PageQuery pageQuery,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "订单类型") @RequestParam(required = false) Integer orderType,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer orderStatus,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo) {
        PageResult<OrderVO> result = orderService.pageOrders(pageQuery, userId, orderType, orderStatus, orderNo);
        return Result.success(result);
    }

    @Operation(summary = "根据用户ID查询订单列表")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('order:list')")
    public Result<List<OrderVO>> getOrdersByUserId(@Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer orderStatus) {
        List<OrderVO> orders = orderService.getOrdersByUserId(userId, orderStatus);
        return Result.success(orders);
    }

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('order:create')")
    public Result<Long> createOrder(@Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "购物车ID列表") @RequestBody List<Long> cartIds,
            @Parameter(description = "收货人") @RequestParam String consignee,
            @Parameter(description = "联系电话") @RequestParam String phone,
            @Parameter(description = "收货地址") @RequestParam String address,
            @Parameter(description = "订单备注") @RequestParam(required = false) String remarks) {
        Long orderId = orderService.createOrder(userId, cartIds, consignee, phone, address, remarks);
        return Result.success("订单创建成功", orderId);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAuthority('order:cancel')")
    public Result<Void> cancelOrder(@Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "取消原因") @RequestParam String reason) {
        orderService.cancelOrder(orderId, reason);
        return Result.success("订单取消成功");
    }

    @Operation(summary = "支付订单")
    @PostMapping("/{orderId}/pay")
    @PreAuthorize("hasAuthority('order:pay')")
    public Result<Void> payOrder(@Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "支付方式") @RequestParam Integer paymentMethod) {
        orderService.payOrder(orderId, paymentMethod);
        return Result.success("订单支付成功");
    }

    @Operation(summary = "审核订单")
    @PostMapping("/{orderId}/audit")
    @PreAuthorize("hasAuthority('order:audit')")
    public Result<Void> auditOrder(@Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "是否通过") @RequestParam boolean approved,
            @Parameter(description = "审核备注") @RequestParam(required = false) String remark) {
        orderService.auditOrder(orderId, approved, remark);
        return Result.success(approved ? "订单审核通过" : "订单审核拒绝");
    }

    @Operation(summary = "发货")
    @PostMapping("/{orderId}/ship")
    @PreAuthorize("hasAuthority('order:ship')")
    public Result<Void> shipOrder(@Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "物流公司") @RequestParam String logisticsCompany,
            @Parameter(description = "物流单号") @RequestParam String trackingNumber,
            @Parameter(description = "发货备注") @RequestParam(required = false) String remark) {
        orderService.shipOrder(orderId, logisticsCompany, trackingNumber, remark);
        return Result.success("订单发货成功");
    }

    @Operation(summary = "完成订单")
    @PostMapping("/{orderId}/complete")
    @PreAuthorize("hasAuthority('order:complete')")
    public Result<Void> completeOrder(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        return Result.success("订单完成");
    }

    @Operation(summary = "申请退款")
    @PostMapping("/{orderId}/refund")
    @PreAuthorize("hasAuthority('order:refund')")
    public Result<Void> refundOrder(@Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "退款原因") @RequestParam String reason) {
        orderService.refundOrder(orderId, reason);
        return Result.success("退款申请提交成功");
    }

    @Operation(summary = "处理退款")
    @PostMapping("/{orderId}/process-refund")
    @PreAuthorize("hasAuthority('order:process-refund')")
    public Result<Void> processRefund(@Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "是否同意") @RequestParam boolean approved,
            @Parameter(description = "处理备注") @RequestParam(required = false) String remark) {
        orderService.processRefund(orderId, approved, remark);
        return Result.success(approved ? "退款处理成功" : "退款拒绝");
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('order:query')")
    public Result<OrderDetailVO> getOrderDetail(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        OrderDetailVO orderDetail = orderService.getOrderDetail(orderId);
        return Result.success(orderDetail);
    }
}
