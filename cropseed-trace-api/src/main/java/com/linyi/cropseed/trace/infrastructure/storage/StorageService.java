package com.linyi.cropseed.trace.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 统一对象存储服务接口
 * 提供文件上传、下载、删除等基础操作
 * 支持多种存储实现（MinIO、OSS、S3等）
 *
 * @author LinYi
 * @since 2025-11-23
 */
public interface StorageService {

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    boolean bucketExists(String bucketName);

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    void createBucket(String bucketName);

    /**
     * 上传文件
     *
     * @param file   文件
     * @param folder 文件夹路径（可选）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 上传文件（使用默认存储桶）
     *
     * @param file 文件
     * @return 文件访问URL
     */
    default String uploadFile(MultipartFile file) {
        return uploadFile(file, null);
    }

    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream downloadFile(String objectName);

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    void deleteFile(String objectName);

    /**
     * 批量删除文件
     *
     * @param objectNames 对象名称列表
     */
    void deleteFiles(List<String> objectNames);

    /**
     * 获取文件预签名URL（用于临时访问）
     *
     * @param objectName 对象名称
     * @param expiry     过期时间（分钟）
     * @return 预签名URL
     */
    String getPresignedUrl(String objectName, int expiry);

    /**
     * 获取文件列表
     *
     * @param prefix 文件前缀（路径）
     * @return 文件名列表
     */
    List<String> listFiles(String prefix);

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称
     * @return 是否存在
     */
    boolean fileExists(String objectName);

    /**
     * 获取文件URL
     *
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    String getFileUrl(String objectName);
}
