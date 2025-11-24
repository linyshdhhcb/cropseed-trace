package com.linyi.cropseed.trace.module.payment.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.payment.model.dto.WxPayDTO;
import com.linyi.cropseed.trace.module.payment.service.WxPayService;
import com.linyi.cropseed.trace.module.payment.model.vo.WxPayVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付控制器
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "微信支付管理", description = "微信支付相关接口")
@RestController
@RequestMapping("/wechat/pay")
@RequiredArgsConstructor
public class WxPayController {

    private final WxPayService wxPayService;

    @Operation(summary = "创建支付订单", description = "创建微信支付订单")
    @PostMapping("/create")
    public Result<WxPayVO> createPayOrder(@Valid @RequestBody WxPayDTO payDTO) {
        WxPayVO payVO = wxPayService.createPayOrder(payDTO);
        return Result.success(payVO);
    }

    @Operation(summary = "支付回调", description = "微信支付结果回调")
    @PostMapping("/notify")
    public String handlePayNotify(@RequestBody String xmlData) {
        // 解析XML数据为Map
        Map<String, String> notifyData = parseXmlToMap(xmlData);
        return wxPayService.handlePayNotify(notifyData);
    }

    /**
     * 解析XML为Map
     */
    private Map<String, String> parseXmlToMap(String xml) {
        Map<String, String> map = new HashMap<>();
        if (xml == null || xml.isEmpty()) {
            return map;
        }

        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern
                    .compile("<(\\w+)>(<!\\[CDATA\\[(.*?)\\]\\]>|(.*?))</\\1>");
            java.util.regex.Matcher matcher = pattern.matcher(xml);

            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
                if (key != null && value != null) {
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            // 忽略解析错误
        }

        return map;
    }

    @Operation(summary = "查询支付结果", description = "查询订单支付状态")
    @GetMapping("/query/{orderNo}")
    public Result<Boolean> queryPayResult(@PathVariable String orderNo) {
        boolean result = wxPayService.queryPayResult(orderNo);
        return Result.success(result);
    }

    @Operation(summary = "申请退款", description = "申请订单退款")
    @PostMapping("/refund")
    public Result<Boolean> refund(@RequestParam String orderNo,
            @RequestParam String refundNo,
            @RequestParam Integer refundAmount,
            @RequestParam String reason) {
        boolean result = wxPayService.refund(orderNo, refundNo, refundAmount, reason);
        return Result.success(result);
    }
}
