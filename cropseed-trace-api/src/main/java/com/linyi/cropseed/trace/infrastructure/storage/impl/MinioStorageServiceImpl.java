package com.linyi.cropseed.trace.infrastructure.storage.impl;

import com.linyi.cropseed.trace.config.properties.MinioProperties;
import com.linyi.cropseed.trace.infrastructure.storage.StorageService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO对象存储服务实现
 *
 * @author LinYi
 * @since 2025-11-23
 */
@Slf4j
@Service("minioStorageService")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "minio", matchIfMissing = true)
public class MinioStorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioConfig;

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("检查MinIO存储桶是否存在失败：{}", e.getMessage());
            return false;
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("创建MinIO存储桶成功：{}", bucketName);
            }
        } catch (Exception e) {
            log.error("创建MinIO存储桶失败：{}", e.getMessage());
            throw new RuntimeException("创建MinIO存储桶失败", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 确保存储桶存在
            String bucketName = minioConfig.getBucketName();
            if (!bucketExists(bucketName)) {
                createBucket(bucketName);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

            // 如果指定了文件夹，则拼接路径
            String objectName = (folder != null && !folder.isEmpty())
                    ? folder + "/" + fileName
                    : fileName;

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("文件上传到MinIO成功：{}", objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("文件上传到MinIO失败：{}", e.getMessage());
            throw new RuntimeException("文件上传到MinIO失败", e);
        }
    }

    @Override
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("从MinIO下载文件失败：{}", e.getMessage());
            throw new RuntimeException("从MinIO下载文件失败", e);
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            log.info("从MinIO删除文件成功：{}", objectName);
        } catch (Exception e) {
            log.error("从MinIO删除文件失败：{}", e.getMessage());
            throw new RuntimeException("从MinIO删除文件失败", e);
        }
    }

    @Override
    public void deleteFiles(List<String> objectNames) {
        try {
            List<DeleteObject> objects = objectNames.stream()
                    .map(DeleteObject::new)
                    .toList();
            
            minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .objects(objects)
                    .build());
            
            log.info("从MinIO批量删除文件成功，数量：{}", objectNames.size());
        } catch (Exception e) {
            log.error("从MinIO批量删除文件失败：{}", e.getMessage());
            throw new RuntimeException("从MinIO批量删除文件失败", e);
        }
    }

    @Override
    public String getPresignedUrl(String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .expiry(expiry, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("获取MinIO文件预签名URL失败：{}", e.getMessage());
            throw new RuntimeException("获取MinIO文件预签名URL失败", e);
        }
    }

    @Override
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
            log.error("获取MinIO文件列表失败：{}", e.getMessage());
            throw new RuntimeException("获取MinIO文件列表失败", e);
        }
        return fileList;
    }

    @Override
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

    @Override
    public String getFileUrl(String objectName) {
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
    }
}
