package com.linyi.cropseed.trace.config;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.tbaas.v20180416.TbaasClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 腾讯云TBaas配置
 *
 * @author linyi
 * @since 2024-01-01
 */
@Slf4j
@Configuration
public class TBaasConfig {

    @Component
    @ConfigurationProperties(prefix = "tencent.tbaas")
    public static class TBaasProperties {
        private String secretId;
        private String secretKey;
        private String region = "ap-beijing";
        private String clusterId = "chainmaker-demo";
        private String chainId = "chain_demo";
        private String contractName = "ChainMakerDemo";
        private Integer timeout = 60000;
        private Retry retry = new Retry();

        public static class Retry {
            private Integer maxAttempts = 3;
            private Long delay = 1000L;

            public Integer getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(Integer maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public Long getDelay() {
                return delay;
            }

            public void setDelay(Long delay) {
                this.delay = delay;
            }
        }

        public String getSecretId() {
            return secretId;
        }

        public void setSecretId(String secretId) {
            this.secretId = secretId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getClusterId() {
            return clusterId;
        }

        public void setClusterId(String clusterId) {
            this.clusterId = clusterId;
        }

        public String getChainId() {
            return chainId;
        }

        public void setChainId(String chainId) {
            this.chainId = chainId;
        }

        public String getContractName() {
            return contractName;
        }

        public void setContractName(String contractName) {
            this.contractName = contractName;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public Retry getRetry() {
            return retry;
        }

        public void setRetry(Retry retry) {
            this.retry = retry;
        }
    }

    @Bean
    public TbaasClient tbaasClient(TBaasProperties properties) {
        try {
            String secretId = properties.getSecretId();
            String secretKey = properties.getSecretKey();

            if (secretId == null || secretKey == null) {
                log.warn("TBaas认证信息未配置，将使用默认凭证");
                // 使用默认凭证提供者
                Credential cred = new Credential();
                return createTbaasClient(cred, properties);
            } else {
                Credential cred = new Credential(secretId, secretKey);
                return createTbaasClient(cred, properties);
            }

        } catch (Exception e) {
            log.error("TBaas客户端初始化失败", e);
            throw new RuntimeException("TBaas客户端初始化失败", e);
        }
    }

    private TbaasClient createTbaasClient(Credential cred, TBaasProperties properties) {
        // HTTP配置
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("tbaas.tencentcloudapi.com");
        httpProfile.setReqMethod("POST");
        httpProfile.setConnTimeout(properties.getTimeout());

        // 客户端配置
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        clientProfile.setSignMethod(ClientProfile.SIGN_TC3_256);

        // 创建客户端
        return new TbaasClient(cred, properties.getRegion(), clientProfile);
    }
}
