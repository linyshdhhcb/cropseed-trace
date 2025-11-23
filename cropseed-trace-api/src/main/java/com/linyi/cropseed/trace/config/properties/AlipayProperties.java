package com.linyi.cropseed.trace.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "pay.alipay")
public class AlipayProperties {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * 服务器地址
     */
    private String serverUrl;

    /**
     * 字符集
     */
    private String charset = "UTF-8";

    /**
     * 签名类型
     */
    private String signType = "RSA2";

    /**
     * 异步通知URL
     */
    private String notifyUrl;

    /**
     * 同步返回URL
     */
    private String returnUrl;
}
