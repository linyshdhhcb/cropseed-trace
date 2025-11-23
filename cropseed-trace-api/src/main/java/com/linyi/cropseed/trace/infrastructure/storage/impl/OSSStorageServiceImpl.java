package com.linyi.cropseed.trace.infrastructure.storage.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import com.linyi.cropseed.trace.infrastructure.storage.StorageService;
import com.linyi.cropseed.trace.config.properties.OSSProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 阿里云OSS对象存储服务实现
 *
 * @author LinYi
 * @since 2025-11-23
 */
@Slf4j
@Service("ossStorageService")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "oss")
public class OSSStorageServiceImpl implements StorageService {

    private final OSS ossClient;
    private final OSSProperties ossConfig;

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            return ossClient.doesBucketExist(bucketName);
        } catch (Exception e) {
            log.error("检查OSS存储桶是否存在失败：{}", e.getMessage());
            return false;
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            if (!bucketExists(bucketName)) {
                CreateBucketRequest request = new CreateBucketRequest(bucketName);
                request.setCannedACL(CannedAccessControlList.PublicRead);
                ossClient.createBucket(request);
                log.info("创建OSS存储桶成功：{}", bucketName);
            }
        } catch (Exception e) {
            log.error("创建OSS存储桶失败：{}", e.getMessage());
            throw new RuntimeException("创建OSS存储桶失败", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 确保存储桶存在
            String bucketName = ossConfig.getBucketName();
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
            PutObjectRequest request = new PutObjectRequest(bucketName, objectName, file.getInputStream());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            request.setMetadata(metadata);

            ossClient.putObject(request);
            log.info("文件上传到OSS成功：{}", objectName);
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("文件上传到OSS失败：{}", e.getMessage());
            throw new RuntimeException("文件上传到OSS失败", e);
        }
    }

    @Override
    public InputStream downloadFile(String objectName) {
        try {
            OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), objectName);
            return ossObject.getObjectContent();
        } catch (Exception e) {
            log.error("从OSS下载文件失败：{}", e.getMessage());
            throw new RuntimeException("从OSS下载文件失败", e);
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            ossClient.deleteObject(ossConfig.getBucketName(), objectName);
            log.info("从OSS删除文件成功：{}", objectName);
        } catch (Exception e) {
            log.error("从OSS删除文件失败：{}", e.getMessage());
            throw new RuntimeException("从OSS删除文件失败", e);
        }
    }

    @Override
    public void deleteFiles(List<String> objectNames) {
        try {
            DeleteObjectsRequest request = new DeleteObjectsRequest(ossConfig.getBucketName());
            request.setKeys(objectNames);
            ossClient.deleteObjects(request);
            log.info("从OSS批量删除文件成功，数量：{}", objectNames.size());
        } catch (Exception e) {
            log.error("从OSS批量删除文件失败：{}", e.getMessage());
            throw new RuntimeException("从OSS批量删除文件失败", e);
        }
    }

    @Override
    public String getPresignedUrl(String objectName, int expiry) {
        try {
            Date expiration = new Date(System.currentTimeMillis() + expiry * 60L * 1000L);
            URL url = ossClient.generatePresignedUrl(ossConfig.getBucketName(), objectName, expiration);
            return url.toString();
        } catch (Exception e) {
            log.error("获取OSS文件预签名URL失败：{}", e.getMessage());
            throw new RuntimeException("获取OSS文件预签名URL失败", e);
        }
    }

    @Override
    public List<String> listFiles(String prefix) {
        List<String> fileList = new ArrayList<>();
        try {
            ListObjectsRequest request = new ListObjectsRequest(ossConfig.getBucketName());
            request.setPrefix(prefix);

            ObjectListing objectListing = ossClient.listObjects(request);
            for (OSSObjectSummary summary : objectListing.getObjectSummaries()) {
                fileList.add(summary.getKey());
            }
        } catch (Exception e) {
            log.error("获取OSS文件列表失败：{}", e.getMessage());
            throw new RuntimeException("获取OSS文件列表失败", e);
        }
        return fileList;
    }

    @Override
    public boolean fileExists(String objectName) {
        try {
            return ossClient.doesObjectExist(ossConfig.getBucketName(), objectName);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectName;
    }
}
