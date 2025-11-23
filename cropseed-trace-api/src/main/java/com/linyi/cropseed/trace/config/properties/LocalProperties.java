package com.linyi.cropseed.trace.config.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 本地文件存储配置属性
 *
 * @author LinYi
 * @since 2025-11-23
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "local")
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class LocalProperties {

    /**
     * 本地存储根目录
     * 默认：项目根目录下的 data/storage
     */
    private String basePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "storage";

    /**
     * HTTP访问URL前缀
     * 用于生成文件访问URL
     * 例如：http://localhost:8085
     */
    private String urlPrefix = "http://localhost:8085";

    /**
     * 默认存储桶名称（对应文件夹）
     */
    private String bucketName = "cropseed-trace";

    /**
     * 是否自动创建目录
     */
    private Boolean autoCreateDirectory = true;

    /**
     * 允许的文件类型（MIME类型）
     * 为空表示不限制
     */
    private String[] allowedContentTypes;

    /**
     * 最大文件大小（字节）
     * 默认：100MB
     */
    private Long maxFileSize = 100 * 1024 * 1024L;

    /**
     * 获取完整的存储根路径
     */
    public String getFullBasePath() {
        return basePath + File.separator + bucketName;
    }
}
