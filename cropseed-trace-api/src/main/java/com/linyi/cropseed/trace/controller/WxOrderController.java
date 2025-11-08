package com.linyi.cropseed.trace.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import com.linyi.cropseed.trace.dto.OrderSubmitGoodsDTO;
import com.linyi.cropseed.trace.dto.WxOrderSubmitDTO;
import com.linyi.cropseed.trace.entity.OrderInfo;
import com.linyi.cropseed.trace.entity.OrderItem;
import com.linyi.cropseed.trace.mapper.OrderInfoMapper;
import com.linyi.cropseed.trace.mapper.OrderItemMapper;
import com.linyi.cropseed.trace.service.OrderService;
import com.linyi.cropseed.trace.service.UserAddressService;
import com.linyi.cropseed.trace.vo.OrderDetailVO;
import com.linyi.cropseed.trace.vo.OrderItemVO;
import com.linyi.cropseed.trace.vo.UserAddressVO;
import com.linyi.cropseed.trace.vo.WxOrderSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 微信小程序订单接口
 */
@Tag(name = "微信订单", description = "微信小程序订单相关接口")
@RestController
@RequestMapping("/wx/order")
@RequiredArgsConstructor
public class WxOrderController {

    private final OrderService orderService;
    private final UserAddressService userAddressService;
    private final OrderItemMapper orderItemMapper;
    private final OrderInfoMapper orderInfoMapper;

    @Operation(summary = "提交订单")
    @PostMapping("/submit")
    public Result<OrderDetailVO> submit(@Valid @RequestBody WxOrderSubmitDTO submitDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<OrderDetailVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        UserAddressVO address = userAddressService.getAddressById(userId, submitDTO.getAddressId());
        if (address == null) {
            return Result.<OrderDetailVO>fail(ResultCode.ADDRESS_NOT_EXIST);
        }

        Long orderId;
        if (CollUtil.isNotEmpty(submitDTO.getCartIds())) {
            orderId = orderService.createOrder(userId, submitDTO.getCartIds(), address.getConsignee(),
                    address.getPhone(), address.getFullAddress(), submitDTO.getRemarks());
        } else if (CollUtil.isNotEmpty(submitDTO.getGoods())) {
            List<OrderSubmitGoodsDTO> goods = submitDTO.getGoods();
            orderId = orderService.createOrderFromGoods(userId, goods, address.getConsignee(), address.getPhone(),
                    address.getFullAddress(), submitDTO.getRemarks());
        } else {
            return Result.fail(ResultCode.ORDER_CREATE_FAIL.getCode(), "请选择商品");
        }

        OrderDetailVO detailVO = orderService.getOrderDetail(orderId);
        return Result.success(detailVO);
    }

    @Operation(summary = "订单列表")
    @GetMapping("/page")
    public Result<PageResult<WxOrderSummaryVO>> page(PageQuery pageQuery,
            @RequestParam(required = false) Integer orderStatus) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<PageResult<WxOrderSummaryVO>>fail(ResultCode.USER_NOT_LOGIN);
        }
        Page<OrderInfo> orderPage = pageOrders(pageQuery, userId, orderStatus);
        List<WxOrderSummaryVO> records = orderPage.getRecords().stream()
                .map(this::convertToSummaryVO)
                .collect(Collectors.toList());
        return Result.success(PageResult.of(orderPage, records));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderId}")
    public Result<OrderDetailVO> detail(@PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<OrderDetailVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (orderInfo == null || !orderInfo.getUserId().equals(userId)) {
            return Result.<OrderDetailVO>fail(ResultCode.ORDER_NOT_EXIST);
        }
        OrderDetailVO vo = orderService.getOrderDetail(orderId);
        return Result.success(vo);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> requestBody) {
        if (SecurityUtils.getCurrentUserId() == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        verifyOwnership(orderId);
        String reason = requestBody != null ? requestBody.get("reason") : null;
        orderService.cancelOrder(orderId, reason != null ? reason : "用户取消订单");
        return Result.success();
    }

    @Operation(summary = "确认收货")
    @PostMapping("/{orderId}/confirm")
    public Result<Void> confirm(@PathVariable Long orderId) {
        if (SecurityUtils.getCurrentUserId() == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        verifyOwnership(orderId);
        orderService.completeOrder(orderId);
        return Result.success();
    }

    @Operation(summary = "申请退款")
    @PostMapping("/{orderId}/refund")
    public Result<Void> refund(@PathVariable Long orderId, @RequestBody Map<String, String> requestBody) {
        if (SecurityUtils.getCurrentUserId() == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        verifyOwnership(orderId);
        String reason = requestBody != null ? requestBody.get("reason") : null;
        orderService.refundOrder(orderId, reason != null ? reason : "用户申请退款");
        return Result.success();
    }

    @Operation(summary = "支付订单")
    @PostMapping("/{orderId}/pay")
    public Result<Void> pay(@PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Integer> requestBody) {
        if (SecurityUtils.getCurrentUserId() == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        verifyOwnership(orderId);
        Integer paymentMethod = requestBody != null ? requestBody.get("paymentMethod") : null;
        orderService.payOrder(orderId, paymentMethod == null ? 1 : paymentMethod);
        return Result.success();
    }

    @Operation(summary = "删除订单")
    @DeleteMapping("/{orderId}")
    public Result<Void> delete(@PathVariable Long orderId) {
        if (SecurityUtils.getCurrentUserId() == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        OrderInfo orderInfo = verifyOwnership(orderId);
        orderInfo.setDeletedFlag(1);
        orderInfoMapper.updateById(orderInfo);
        return Result.success();
    }

    private OrderInfo verifyOwnership(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (orderInfo == null || !orderInfo.getUserId().equals(userId)) {
            throw new com.linyi.cropseed.trace.common.exception.BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        return orderInfo;
    }

    private Page<OrderInfo> pageOrders(PageQuery pageQuery, Long userId, Integer orderStatus) {
        Page<OrderInfo> page = pageQuery.toMpPageWithOrder();
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getUserId, userId)
                .eq(orderStatus != null, OrderInfo::getOrderStatus, orderStatus);
        return orderInfoMapper.selectPage(page, wrapper);
    }

    private WxOrderSummaryVO convertToSummaryVO(OrderInfo orderInfo) {
        WxOrderSummaryVO vo = BeanUtil.copyProperties(orderInfo, WxOrderSummaryVO.class);
        vo.setOrderId(orderInfo.getId());
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderInfo.getId());
        List<OrderItem> orderItems = orderItemMapper.selectList(wrapper);
        vo.setItems(orderItems.stream().map(item -> BeanUtil.copyProperties(item, OrderItemVO.class))
                .collect(Collectors.toList()));
        return vo;
    }
}
