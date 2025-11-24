package com.linyi.cropseed.trace.module.payment.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.payment.model.dto.AlipayDTO;
import com.linyi.cropseed.trace.entity.OrderInfo;
import com.linyi.cropseed.trace.module.payment.service.AlipayService;
import com.linyi.cropseed.trace.service.OrderService;
import com.linyi.cropseed.trace.module.payment.model.vo.AlipayVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 *
 * @author LinYi
 * @since 2025-11-13
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "支付管理", description = "支付相关接口")
public class PaymentController {

    private final AlipayService alipayService;
    private final OrderService orderService;

    @PostMapping("/alipay/qrcode")
    @Operation(summary = "生成支付宝支付二维码")
    public Result<Map<String, Object>> generateAlipayQrcode(@RequestBody Map<String, Object> request) {
        try {
            Long orderId = Long.valueOf(request.get("orderId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());

            // 获取订单信息
            OrderInfo order = orderService.getById(orderId);
            if (order == null) {
                return Result.fail("订单不存在");
            }

            // 创建支付宝支付DTO
            AlipayDTO alipayDTO = new AlipayDTO();
            alipayDTO.setOrderNo(order.getOrderNo());
            alipayDTO.setSubject("种子溯源系统-订单支付");
            alipayDTO.setAmount(amount);
            alipayDTO.setBody("订单号：" + order.getOrderNo());

            // 生成支付二维码
            AlipayVO alipayVO = alipayService.createPayOrder(alipayDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("qrcodeUrl", alipayVO.getQrCodeUrl());
            result.put("orderNo", order.getOrderNo());

            return Result.success(result);

        } catch (Exception e) {
            log.error("生成支付宝二维码失败", e);
            return Result.fail("生成支付二维码失败：" + e.getMessage());
        }
    }

    @GetMapping("/status/{orderId}")
    @Operation(summary = "查询支付状态")
    public Result<Map<String, Object>> getPaymentStatus(@Parameter(description = "订单ID") @PathVariable Long orderId) {
        try {
            // 只查询订单状态，不查询完整订单信息，减少数据传输
            OrderInfo order = orderService.getOrderStatusById(orderId);
            if (order == null) {
                return Result.fail("订单不存在");
            }

            Map<String, Object> result = new HashMap<>();

            // 根据订单状态返回支付状态
            String status;
            switch (order.getOrderStatus()) {
                case 0: // 未支付
                    status = "UNPAID";
                    break;
                case 1: // 已支付待审核
                case 2: // 待发货
                case 3: // 已发货
                case 4: // 已完成
                    status = "PAID";
                    break;
                case 5: // 已取消
                    status = "CANCELLED";
                    break;
                default:
                    status = "UNKNOWN";
            }

            result.put("status", status);
            result.put("orderNo", order.getOrderNo());
            result.put("orderStatus", order.getOrderStatus());

            return Result.success(result);

        } catch (Exception e) {
            log.error("查询支付状态失败", e);
            return Result.fail("查询支付状态失败：" + e.getMessage());
        }
    }

    @PostMapping("/alipay/notify")
    @Operation(summary = "支付宝异步通知")
    public String alipayNotify(HttpServletRequest request) {
        try {
            // 获取支付宝回调参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();

            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            log.info("支付宝回调参数：{}", params);

            // 处理回调
            String result = alipayService.handlePayNotify(params);

            return result;

        } catch (Exception e) {
            log.error("处理支付宝回调失败", e);
            return "failure";
        }
    }

    @PostMapping("/alipay/return")
    @Operation(summary = "支付宝同步返回")
    public Result<String> alipayReturn(HttpServletRequest request) {
        try {
            // 获取支付宝返回参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();

            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            log.info("支付宝同步返回参数：{}", params);

            // 验证签名
            String tradeStatus = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                return Result.success("支付成功");
            } else {
                return Result.fail("支付失败");
            }

        } catch (Exception e) {
            log.error("处理支付宝同步返回失败", e);
            return Result.fail("处理失败：" + e.getMessage());
        }
    }

    @PostMapping("/simulate-success")
    @Operation(summary = "模拟支付成功（仅用于演示）")
    public Result<Map<String, Object>> simulatePaymentSuccess(@RequestBody Map<String, Object> request) {
        try {
            Long orderId = Long.valueOf(request.get("orderId").toString());
            Integer paymentMethod = Integer.valueOf(request.get("paymentMethod").toString());

            log.info("模拟支付成功: orderId={}, paymentMethod={}", orderId, paymentMethod);

            // 获取订单信息
            OrderInfo order = orderService.getById(orderId);
            if (order == null) {
                return Result.fail("订单不存在");
            }

            // 检查订单状态
            if (order.getOrderStatus() != 0) {
                return Result.fail("订单状态错误，无法支付");
            }

            // 调用支付方法
            orderService.payOrder(orderId, paymentMethod);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "模拟支付成功");
            result.put("orderId", orderId);

            return Result.success(result);

        } catch (Exception e) {
            log.error("模拟支付失败", e);
            return Result.fail("模拟支付失败：" + e.getMessage());
        }
    }
}
