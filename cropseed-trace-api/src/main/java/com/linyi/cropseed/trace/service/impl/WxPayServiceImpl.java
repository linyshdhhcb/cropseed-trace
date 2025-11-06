package com.linyi.cropseed.trace.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.dto.WxPayDTO;
import com.linyi.cropseed.trace.entity.OrderInfo;
import com.linyi.cropseed.trace.service.OrderService;
import com.linyi.cropseed.trace.service.WxPayService;
import com.linyi.cropseed.trace.vo.WxPayVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 微信支付服务实现
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxPayServiceImpl implements WxPayService {

    private final OrderService orderService;

    @Value("${wxpay.appid:}")
    private String appId;

    @Value("${wxpay.mchid:}")
    private String mchId;

    @Value("${wxpay.key:}")
    private String apiKey;

    @Value("${wxpay.notify-url:}")
    private String notifyUrl;

    @Override
    public WxPayVO createPayOrder(WxPayDTO payDTO) {
        try {
            // 构建统一下单参数
            Map<String, String> params = new HashMap<>();
            params.put("appid", appId);
            params.put("mch_id", mchId);
            params.put("nonce_str", IdUtil.fastSimpleUUID());
            params.put("body", payDTO.getDescription());
            params.put("out_trade_no", payDTO.getOrderNo());
            params.put("total_fee", payDTO.getAmount().multiply(new BigDecimal("100")).intValue() + "");
            params.put("spbill_create_ip", "127.0.0.1");
            params.put("notify_url", StrUtil.isNotBlank(payDTO.getNotifyUrl()) ? payDTO.getNotifyUrl() : notifyUrl);
            params.put("trade_type", "JSAPI");
            params.put("openid", payDTO.getOpenid());

            // 生成签名
            String sign = generateSign(params);
            params.put("sign", sign);

            // 调用微信统一下单API
            String xml = mapToXml(params);
            String response = HttpUtil.post("https://api.mch.weixin.qq.com/pay/unifiedorder", xml);

            // 解析响应
            Map<String, String> responseMap = xmlToMap(response);
            if (!"SUCCESS".equals(responseMap.get("return_code"))) {
                throw new BusinessException(ResultCode.WECHAT_PAY_FAIL, responseMap.get("return_msg"));
            }

            if (!"SUCCESS".equals(responseMap.get("result_code"))) {
                throw new BusinessException(ResultCode.WECHAT_PAY_FAIL, responseMap.get("err_code_des"));
            }

            // 构建小程序支付参数
            WxPayVO payVO = new WxPayVO();
            payVO.setPrepayId(responseMap.get("prepay_id"));
            payVO.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
            payVO.setNonceStr(IdUtil.fastSimpleUUID());
            payVO.setSignType("MD5");
            payVO.setPackageValue("prepay_id=" + responseMap.get("prepay_id"));

            // 生成小程序支付签名
            Map<String, String> payParams = new HashMap<>();
            payParams.put("appId", appId);
            payParams.put("timeStamp", payVO.getTimeStamp());
            payParams.put("nonceStr", payVO.getNonceStr());
            payParams.put("package", payVO.getPackageValue());
            payParams.put("signType", payVO.getSignType());

            String paySign = generateSign(payParams);
            payVO.setPaySign(paySign);

            return payVO;
        } catch (Exception e) {
            log.error("创建微信支付订单失败", e);
            throw new BusinessException(ResultCode.WECHAT_PAY_FAIL, "创建支付订单失败");
        }
    }

    @Override
    public String handlePayNotify(Map<String, String> notifyData) {
        try {
            // 验证签名
            if (!verifySign(notifyData)) {
                return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[签名验证失败]]></return_msg></xml>";
            }

            String orderNo = notifyData.get("out_trade_no");
            String transactionId = notifyData.get("transaction_id");
            String resultCode = notifyData.get("result_code");

            if ("SUCCESS".equals(resultCode)) {
                // 支付成功，更新订单状态
                log.info("订单支付成功: orderNo={}, transactionId={}", orderNo, transactionId);
                // 更新订单状态为已支付
                // 先根据订单号查询订单ID
                OrderInfo orderInfo = orderService.getOne(new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getOrderNo, orderNo));
                if (orderInfo != null) {
                    orderService.updateOrderStatus(orderInfo.getId(), 4, "微信支付成功", "订单支付完成");
                }
            } else {
                log.warn("订单支付失败: orderNo={}, errCode={}", orderNo, notifyData.get("err_code"));
            }

            return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>";
        }
    }

    @Override
    public boolean queryPayResult(String orderNo) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("appid", appId);
            params.put("mch_id", mchId);
            params.put("out_trade_no", orderNo);
            params.put("nonce_str", IdUtil.fastSimpleUUID());

            String sign = generateSign(params);
            params.put("sign", sign);

            String xml = mapToXml(params);
            String response = HttpUtil.post("https://api.mch.weixin.qq.com/pay/orderquery", xml);

            Map<String, String> responseMap = xmlToMap(response);
            return "SUCCESS".equals(responseMap.get("return_code")) &&
                    "SUCCESS".equals(responseMap.get("result_code")) &&
                    "SUCCESS".equals(responseMap.get("trade_state"));
        } catch (Exception e) {
            log.error("查询支付结果失败", e);
            return false;
        }
    }

    @Override
    public boolean refund(String orderNo, String refundNo, Integer refundAmount, String reason) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("appid", appId);
            params.put("mch_id", mchId);
            params.put("nonce_str", IdUtil.fastSimpleUUID());
            params.put("out_trade_no", orderNo);
            params.put("out_refund_no", refundNo);
            params.put("total_fee", "100"); // 需要从订单中获取
            params.put("refund_fee", refundAmount.toString());

            String sign = generateSign(params);
            params.put("sign", sign);

            String xml = mapToXml(params);
            String response = HttpUtil.post("https://api.mch.weixin.qq.com/secapi/pay/refund", xml);

            Map<String, String> responseMap = xmlToMap(response);
            return "SUCCESS".equals(responseMap.get("return_code")) &&
                    "SUCCESS".equals(responseMap.get("result_code"));
        } catch (Exception e) {
            log.error("申请退款失败", e);
            return false;
        }
    }

    /**
     * 生成签名
     */
    private String generateSign(Map<String, String> params) {
        // 过滤空值并排序
        TreeMap<String, String> sortedParams = new TreeMap<>();
        params.forEach((key, value) -> {
            if (StrUtil.isNotBlank(value) && !"sign".equals(key)) {
                sortedParams.put(key, value);
            }
        });

        // 拼接字符串
        StringBuilder sb = new StringBuilder();
        sortedParams.forEach((key, value) -> sb.append(key).append("=").append(value).append("&"));
        sb.append("key=").append(apiKey);

        // MD5加密并转大写
        return MD5.create().digestHex(sb.toString()).toUpperCase();
    }

    /**
     * 验证签名
     */
    private boolean verifySign(Map<String, String> params) {
        String sign = params.get("sign");
        if (StrUtil.isBlank(sign)) {
            return false;
        }
        String calculatedSign = generateSign(params);
        return sign.equals(calculatedSign);
    }

    /**
     * Map转XML
     */
    private String mapToXml(Map<String, String> params) {
        StringBuilder xml = new StringBuilder("<xml>");
        params.forEach((key, value) -> xml.append("<").append(key).append("><![CDATA[").append(value).append("]]></")
                .append(key).append(">"));
        xml.append("</xml>");
        return xml.toString();
    }

    /**
     * XML转Map
     */
    private Map<String, String> xmlToMap(String xml) {
        Map<String, String> map = new HashMap<>();
        if (StrUtil.isBlank(xml)) {
            return map;
        }

        try {
            // 使用正则表达式解析XML
            // 匹配 <key><![CDATA[value]]></key> 或 <key>value</key>
            java.util.regex.Pattern pattern = java.util.regex.Pattern
                    .compile("<(\\w+)>(<!\\[CDATA\\[(.*?)\\]\\]>|(.*?))</\\1>");
            java.util.regex.Matcher matcher = pattern.matcher(xml);

            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(3); // CDATA内容
                if (StrUtil.isBlank(value)) {
                    value = matcher.group(4); // 普通内容
                }
                if (StrUtil.isNotBlank(key) && StrUtil.isNotBlank(value)) {
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            log.error("XML解析失败", e);
        }

        return map;
    }
}
