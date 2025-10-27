package com.linyi.cropseed.trace.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件存储服务
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final com.linyi.cropseed.trace.config.MinioConfig minioConfig;

    /**
     * 检查存储桶是否存在
     */
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("检查存储桶是否存在失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 创建存储桶
     */
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("创建存储桶成功：{}", bucketName);
            }
        } catch (Exception e) {
            log.error("创建存储桶失败：{}", e.getMessage());
            throw new RuntimeException("创建存储桶失败", e);
        }
    }

    /**
     * 上传文件
     * 
     * @param file   文件
     * @param folder 文件夹路径（可选）
     * @return 文件URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 确保存储桶存在
            String bucketName = minioConfig.getBucketName();
            if (!bucketExists(bucketName)) {
                createBucket(bucketName);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

            // 如果指定了文件夹，则拼接路径
            String objectName = folder != null && !folder.isEmpty()
                    ? folder + "/" + fileName
                    : fileName;

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("文件上传成功：{}", objectName);
            return minioConfig.getEndpoint() + "/" + bucketName + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 上传文件（使用默认存储桶）
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, null);
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("文件下载失败：{}", e.getMessage());
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            log.info("文件删除成功：{}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败：{}", e.getMessage());
            throw new RuntimeException("文件删除失败", e);
        }
    }

    /**
     * 获取文件预签名URL（用于临时访问）
     * 
     * @param objectName 对象名称
     * @param expiry     过期时间（分钟）
     */
    public String getPresignedUrl(String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .expiry(expiry, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("获取文件预签名URL失败：{}", e.getMessage());
            throw new RuntimeException("获取文件预签名URL失败", e);
        }
    }

    /**
     * 获取文件列表
     */
    public List<String> listFiles(String prefix) {
        List<String> fileList = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .prefix(prefix)
                    .recursive(true)
                    .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                fileList.add(item.objectName());
            }
        } catch (Exception e) {
            log.error("获取文件列表失败：{}", e.getMessage());
            throw new RuntimeException("获取文件列表失败", e);
        }
        return fileList;
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
