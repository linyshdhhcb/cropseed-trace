package com.linyi.cropseed.trace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wx")
public class WxConfig {

    /**
     * 小程序AppID
     */
    private String appId;

    /**
     * 小程序AppSecret
     */
    private String appSecret;

    /**
     * 微信支付配置
     */
    private WxPayConfig pay = new WxPayConfig();

    @Data
    public static class WxPayConfig {
        /**
         * 商户号
         */
        private String mchId;

        /**
         * API密钥
         */
        private String key;

        /**
         * 支付回调地址
         */
        private String notifyUrl;

        /**
         * 证书路径
         */
        private String certPath;
    }
}
