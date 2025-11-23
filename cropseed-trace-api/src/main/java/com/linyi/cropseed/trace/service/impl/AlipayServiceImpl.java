package com.linyi.cropseed.trace.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.QRCodeUtil;
import com.linyi.cropseed.trace.config.properties.AlipayProperties;
import com.linyi.cropseed.trace.dto.AlipayDTO;
import com.linyi.cropseed.trace.service.AlipayService;
import com.linyi.cropseed.trace.service.OrderService;
import com.linyi.cropseed.trace.entity.OrderInfo;
import com.linyi.cropseed.trace.vo.AlipayVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付服务实现
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayServiceImpl implements AlipayService {

    private final AlipayProperties alipayConfig;
    private final OrderService orderService;

    @Override
    public AlipayVO createPayOrder(AlipayDTO payDTO) {
        try {
            // 创建支付宝客户端（沙箱环境）
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getServerUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    alipayConfig.getCharset(),
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getSignType());

            // 使用当面付预下单接口
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();

            // 正确设置业务参数
            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", payDTO.getOrderNo());
            bizContent.put("total_amount", payDTO.getAmount().toString());
            bizContent.put("subject", payDTO.getSubject());
            if (StrUtil.isNotBlank(payDTO.getBody())) {
                bizContent.put("body", payDTO.getBody());
            }

            request.setBizContent(JSON.toJSONString(bizContent));

            // 设置回调地址（可选）
            request.setNotifyUrl(StrUtil.isNotBlank(payDTO.getNotifyUrl()) ?
                    payDTO.getNotifyUrl() : alipayConfig.getNotifyUrl());

            // 调用支付接口
            AlipayTradePrecreateResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                AlipayVO payVO = new AlipayVO();
                payVO.setOrderNo(payDTO.getOrderNo());

                // 获取二维码内容
                String qrCodeUrl = response.getQrCode();
                payVO.setPayUrl(qrCodeUrl);

                // 生成二维码Base64
                String qrCode = QRCodeUtil.generateQRCodeBase64(qrCodeUrl);
                payVO.setQrCodeUrl(qrCode);

                log.info("支付宝当面付二维码生成成功: {}", qrCodeUrl);
                return payVO;
            } else {
                throw new BusinessException(ResultCode.ALIPAY_FAILED, response.getMsg() + ":" + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付订单失败", e);
            throw new BusinessException(ResultCode.ALIPAY_FAILED, "创建支付订单失败: " + e.getErrMsg());
        }
    }


    @Override
    public AlipayVO createPayOrderMobile(AlipayDTO payDTO) {
        try {
            // 创建支付宝客户端
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getServerUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    alipayConfig.getCharset(),
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getSignType());

            // 创建手机支付请求
            AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
            request.setReturnUrl(
                    StrUtil.isNotBlank(payDTO.getReturnUrl()) ? payDTO.getReturnUrl() : alipayConfig.getReturnUrl());
            request.setNotifyUrl(
                    StrUtil.isNotBlank(payDTO.getNotifyUrl()) ? payDTO.getNotifyUrl() : alipayConfig.getNotifyUrl());

            // 设置业务参数
            AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
            model.setOutTradeNo(payDTO.getOrderNo());
            model.setTotalAmount(payDTO.getAmount().toString());
            model.setSubject(payDTO.getSubject());
            model.setBody(payDTO.getBody());
            model.setProductCode("QUICK_WAP_WAY");

            request.setBizModel(model);

            // 调用支付接口
            AlipayTradeWapPayResponse response = alipayClient.pageExecute(request);

            if (response.isSuccess()) {
                AlipayVO payVO = new AlipayVO();
                payVO.setOrderNo(payDTO.getOrderNo());
                payVO.setPayForm(response.getBody());
                return payVO;
            } else {
                throw new BusinessException(ResultCode.ALIPAY_FAILED, response.getMsg());
            }
        } catch (AlipayApiException e) {
            log.error("创建支付宝手机支付订单失败", e);
            throw new BusinessException(ResultCode.ALIPAY_FAILED, "创建支付订单失败: " + e.getErrMsg());
        }
    }

    @Override
    public String handlePayNotify(Map<String, String> notifyData) {
        try {
            // 验证签名
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    notifyData,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType());

            if (!signVerified) {
                log.warn("支付宝回调签名验证失败");
                return "failure";
            }

            String orderNo = notifyData.get("out_trade_no");
            String tradeNo = notifyData.get("trade_no");
            String tradeStatus = notifyData.get("trade_status");

            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 支付成功，更新订单状态
                log.info("订单支付成功: orderNo={}, tradeNo={}", orderNo, tradeNo);
                // 先根据订单号查询订单ID
                OrderInfo orderInfo = orderService.getOne(new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getOrderNo, orderNo));
                if (orderInfo != null && orderInfo.getOrderStatus() == 0) {
                    // 调用支付方法，触发库存扣减等逻辑
                    orderService.payOrder(orderInfo.getId(), 2); // 2表示支付宝支付
                    log.info("支付宝支付成功，订单状态已更新: orderId={}", orderInfo.getId());
                }
                return "success";
            } else {
                log.warn("订单支付失败: orderNo={}, tradeStatus={}", orderNo, tradeStatus);
                return "failure";
            }
        } catch (Exception e) {
            log.error("处理支付宝回调失败", e);
            return "failure";
        }
    }

    @Override
    public boolean queryPayResult(String orderNo) {
        try {
            // 创建支付宝客户端
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getServerUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    alipayConfig.getCharset(),
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getSignType());

            // 创建查询请求
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");

            // 调用查询接口
            AlipayTradeQueryResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                String tradeStatus = response.getTradeStatus();
                return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
            } else {
                log.warn("查询支付结果失败: {}", response.getMsg());
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("查询支付结果失败", e);
            return false;
        }
    }

    @Override
    public boolean refund(String orderNo, String refundNo, BigDecimal refundAmount, String reason) {
        try {
            // 创建支付宝客户端
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getServerUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    alipayConfig.getCharset(),
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getSignType());

            // 创建退款请求
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            request.setBizContent("{\"out_trade_no\":\"" + orderNo +
                    "\",\"out_request_no\":\"" + refundNo +
                    "\",\"refund_amount\":\"" + refundAmount +
                    "\",\"refund_reason\":\"" + reason + "\"}");

            // 调用退款接口
            AlipayTradeRefundResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                log.info("退款成功: orderNo={}, refundNo={}, amount={}", orderNo, refundNo, refundAmount);
                return true;
            } else {
                log.warn("退款失败: {}", response.getMsg());
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("申请退款失败", e);
            return false;
        }
    }

    @Override
    public boolean closeOrder(String orderNo) {
        // 实现关闭订单逻辑
        try {
            // 创建支付宝客户端
            AlipayClient alipayClient = new DefaultAlipayClient(
                    alipayConfig.getServerUrl(),
                    alipayConfig.getAppId(),
                    alipayConfig.getPrivateKey(),
                    "json",
                    alipayConfig.getCharset(),
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getSignType());

            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");
            AlipayTradeCloseResponse response = alipayClient.execute(request);
            return response.isSuccess();
        } catch (Exception e) {
            log.error("关闭订单失败: orderNo={}", orderNo, e);
            return false;
        }
    }

}
