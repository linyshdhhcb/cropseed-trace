package com.linyi.cropseed.trace.infrastructure.storage.enums;

import lombok.Getter;

/**
 * 存储类型枚举
 *
 * @author LinYi
 * @since 2025-11-23
 */
@Getter
public enum StorageType {

    /**
     * MinIO对象存储
     */
    MINIO("minio", "MinIO对象存储"),

    /**
     * 阿里云OSS
     */
    OSS("oss", "阿里云OSS"),

    /**
     * 腾讯云COS
     */
    COS("cos", "腾讯云COS"),

    /**
     * AWS S3
     */
    S3("s3", "AWS S3"),

    /**
     * 本地文件系统
     */
    LOCAL("local", "本地文件系统");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    StorageType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static StorageType fromCode(String code) {
        for (StorageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的存储类型：" + code);
    }
}
