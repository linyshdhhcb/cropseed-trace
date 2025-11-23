package com.linyi.cropseed.trace.config.properties;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置属性
 *
 * @author LinYi
 * @since 2025-11-23
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "oss")
@ConditionalOnProperty(name = "storage.type", havingValue = "oss")
public class OSSProperties {

    /**
     * OSS服务端点（如：oss-cn-beijing.aliyuncs.com）
     */
    private String endpoint;

    /**
     * 访问密钥ID
     */
    private String accessKeyId;

    /**
     * 访问密钥密码
     */
    private String accessKeySecret;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 创建OSS客户端Bean
     */
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "oss")
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
