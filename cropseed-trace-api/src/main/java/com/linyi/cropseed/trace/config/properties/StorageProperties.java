package com.linyi.cropseed.trace.config.properties;

import com.linyi.cropseed.trace.infrastructure.storage.enums.StorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 对象存储统一配置属性
 *
 * @author LinYi
 * @since 2025-11-23
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * 存储类型（minio, oss, cos, s3, local）
     * 默认使用MinIO
     */
    private String type = "minio";

    /**
     * 是否启用存储功能
     */
    private Boolean enabled = true;

    /**
     * 默认存储桶名称
     */
    private String defaultBucket = "cropseed-trace";

    /**
     * 获取存储类型枚举
     */
    public StorageType getStorageType() {
        return StorageType.fromCode(type);
    }
}
