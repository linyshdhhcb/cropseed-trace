package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.dto.AlipayDTO;
import com.linyi.cropseed.trace.service.AlipayService;
import com.linyi.cropseed.trace.vo.AlipayVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付控制器
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "支付宝支付管理", description = "支付宝支付相关接口")
@RestController
@RequestMapping("/api/alipay")
@RequiredArgsConstructor
public class AlipayController {

    private final AlipayService alipayService;

    @Operation(summary = "创建支付订单（网页）", description = "创建支付宝网页支付订单")
    @PostMapping("/create")
    public Result<AlipayVO> createPayOrder(@Valid @RequestBody AlipayDTO payDTO) {
        AlipayVO payVO = alipayService.createPayOrder(payDTO);
        return Result.success(payVO);
    }

    @Operation(summary = "创建支付订单（手机）", description = "创建支付宝手机支付订单")
    @PostMapping("/create-mobile")
    public Result<AlipayVO> createPayOrderMobile(@Valid @RequestBody AlipayDTO payDTO) {
        AlipayVO payVO = alipayService.createPayOrderMobile(payDTO);
        return Result.success(payVO);
    }

    @Operation(summary = "支付回调", description = "支付宝支付结果回调")
    @PostMapping("/notify")
    public String handlePayNotify(@RequestParam Map<String, String> notifyData) {
        return alipayService.handlePayNotify(notifyData);
    }

    @Operation(summary = "查询支付结果", description = "查询订单支付状态")
    @GetMapping("/query/{orderNo}")
    public Result<Boolean> queryPayResult(@PathVariable String orderNo) {
        boolean result = alipayService.queryPayResult(orderNo);
        return Result.success(result);
    }

    @Operation(summary = "申请退款", description = "申请订单退款")
    @PostMapping("/refund")
    public Result<Boolean> refund(@RequestParam String orderNo,
            @RequestParam String refundNo,
            @RequestParam BigDecimal refundAmount,
            @RequestParam String reason) {
        boolean result = alipayService.refund(orderNo, refundNo, refundAmount, reason);
        return Result.success(result);
    }

    @Operation(summary = "关闭订单", description = "关闭支付订单")
    @PostMapping("/close/{orderNo}")
    public Result<Boolean> closeOrder(@PathVariable String orderNo) {
        boolean result = alipayService.closeOrder(orderNo);
        return Result.success(result);
    }
}
