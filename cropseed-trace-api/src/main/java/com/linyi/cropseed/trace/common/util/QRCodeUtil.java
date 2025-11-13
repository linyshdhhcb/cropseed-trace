package com.linyi.cropseed.trace.common.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 * 
 * @author LinYi
 * @since 2025-11-13
 */
@Slf4j
public class QRCodeUtil {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;

    /**
     * 生成二维码Base64字符串
     * 
     * @param content 二维码内容
     * @return Base64编码的PNG图片
     */
    public static String generateQRCodeBase64(String content) {
        return generateQRCodeBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码Base64字符串
     * 
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @return Base64编码的PNG图片
     */
    public static String generateQRCodeBase64(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            
            // 设置二维码参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            // 生成二维码矩阵
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            
            // 转换为图片
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            // 转换为Base64
            byte[] imageBytes = outputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            return "data:image/png;base64," + base64Image;
            
        } catch (WriterException | IOException e) {
            log.error("生成二维码失败", e);
            throw new RuntimeException("生成二维码失败: " + e.getMessage());
        }
    }

    /**
     * 生成支付宝支付二维码
     * 
     * @param payUrl 支付URL
     * @return Base64编码的二维码图片
     */
    public static String generateAlipayQRCode(String payUrl) {
        // 提取支付URL中的关键信息生成二维码
        // 这里简化处理，实际项目中需要根据支付宝的具体要求来处理
        return generateQRCodeBase64(payUrl, 300, 300);
    }
}
