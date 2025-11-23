package com.linyi.cropseed.trace.infrastructure.storage.impl;

import com.linyi.cropseed.trace.infrastructure.storage.StorageService;
import com.linyi.cropseed.trace.config.properties.LocalProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 本地文件存储服务实现
 *
 * @author LinYi
 * @since 2025-11-23
 */
@Slf4j
@Service("localStorageService")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class LocalStorageServiceImpl implements StorageService {

    private final LocalProperties localConfig;

    /**
     * 初始化时创建存储目录
     */
    @PostConstruct
    public void init() {
        if (localConfig.getAutoCreateDirectory()) {
            try {
                Path basePath = Paths.get(localConfig.getBasePath());
                if (!Files.exists(basePath)) {
                    Files.createDirectories(basePath);
                    log.info("创建本地存储根目录：{}", basePath);
                }

                Path bucketPath = Paths.get(localConfig.getFullBasePath());
                if (!Files.exists(bucketPath)) {
                    Files.createDirectories(bucketPath);
                    log.info("创建默认存储桶目录：{}", bucketPath);
                }
            } catch (IOException e) {
                log.error("创建本地存储目录失败", e);
                throw new RuntimeException("初始化本地存储失败", e);
            }
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            Path bucketPath = Paths.get(localConfig.getBasePath(), bucketName);
            return Files.exists(bucketPath) && Files.isDirectory(bucketPath);
        } catch (Exception e) {
            log.error("检查本地存储桶是否存在失败：{}", e.getMessage());
            return false;
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            Path bucketPath = Paths.get(localConfig.getBasePath(), bucketName);
            if (!Files.exists(bucketPath)) {
                Files.createDirectories(bucketPath);
                log.info("创建本地存储桶成功：{}", bucketName);
            }
        } catch (IOException e) {
            log.error("创建本地存储桶失败：{}", e.getMessage());
            throw new RuntimeException("创建本地存储桶失败", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 验证文件
            validateFile(file);

            // 确保存储桶存在
            String bucketName = localConfig.getBucketName();
            if (!bucketExists(bucketName)) {
                createBucket(bucketName);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

            // 构建文件路径
            String relativePath = buildRelativePath(folder, fileName);
            Path filePath = Paths.get(localConfig.getBasePath(), bucketName, relativePath);

            // 确保父目录存在
            Path parentDir = filePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // 保存文件
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("文件保存到本地成功：{}", filePath);
            return getFileUrl(relativePath);
        } catch (IOException e) {
            log.error("文件保存到本地失败：{}", e.getMessage());
            throw new RuntimeException("文件保存到本地失败", e);
        }
    }

    @Override
    public InputStream downloadFile(String objectName) {
        try {
            Path filePath = Paths.get(localConfig.getFullBasePath(), objectName);
            if (!Files.exists(filePath)) {
                throw new FileNotFoundException("文件不存在：" + objectName);
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            log.error("从本地读取文件失败：{}", e.getMessage());
            throw new RuntimeException("从本地读取文件失败", e);
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            Path filePath = Paths.get(localConfig.getFullBasePath(), objectName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("从本地删除文件成功：{}", objectName);

                // 删除空的父目录
                deleteEmptyParentDirs(filePath.getParent());
            }
        } catch (IOException e) {
            log.error("从本地删除文件失败：{}", e.getMessage());
            throw new RuntimeException("从本地删除文件失败", e);
        }
    }

    @Override
    public void deleteFiles(List<String> objectNames) {
        for (String objectName : objectNames) {
            deleteFile(objectName);
        }
        log.info("从本地批量删除文件成功，数量：{}", objectNames.size());
    }

    @Override
    public String getPresignedUrl(String objectName, int expiry) {
        // 本地存储不需要预签名，直接返回访问URL
        log.warn("本地存储不支持预签名URL，返回普通访问URL");
        return getFileUrl(objectName);
    }

    @Override
    public List<String> listFiles(String prefix) {
        List<String> fileList = new ArrayList<>();
        try {
            Path searchPath = StringUtils.hasText(prefix)
                    ? Paths.get(localConfig.getFullBasePath(), prefix)
                    : Paths.get(localConfig.getFullBasePath());

            if (!Files.exists(searchPath)) {
                return fileList;
            }

            try (Stream<Path> paths = Files.walk(searchPath)) {
                paths.filter(Files::isRegularFile)
                        .forEach(path -> {
                            Path basePath = Paths.get(localConfig.getFullBasePath());
                            String relativePath = basePath.relativize(path).toString();
                            // 统一使用正斜杠
                            fileList.add(relativePath.replace("\\", "/"));
                        });
            }
        } catch (IOException e) {
            log.error("获取本地文件列表失败：{}", e.getMessage());
            throw new RuntimeException("获取本地文件列表失败", e);
        }
        return fileList;
    }

    @Override
    public boolean fileExists(String objectName) {
        try {
            Path filePath = Paths.get(localConfig.getFullBasePath(), objectName);
            return Files.exists(filePath) && Files.isRegularFile(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        // 构建HTTP访问URL
        String urlPrefix = localConfig.getUrlPrefix();
        if (!urlPrefix.endsWith("/")) {
            urlPrefix += "/";
        }
        // 统一使用正斜杠
        String normalizedPath = objectName.replace("\\", "/");
        return urlPrefix + "api/storage/files/" + localConfig.getBucketName() + "/" + normalizedPath;
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > localConfig.getMaxFileSize()) {
            throw new IllegalArgumentException("文件大小超过限制：" + localConfig.getMaxFileSize() + " 字节");
        }

        // 检查文件类型
        String[] allowedTypes = localConfig.getAllowedContentTypes();
        if (allowedTypes != null && allowedTypes.length > 0) {
            String contentType = file.getContentType();
            boolean allowed = false;
            for (String allowedType : allowedTypes) {
                if (allowedType.equals(contentType)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                throw new IllegalArgumentException("不允许的文件类型：" + contentType);
            }
        }
    }

    /**
     * 构建相对路径
     */
    private String buildRelativePath(String folder, String fileName) {
        if (StringUtils.hasText(folder)) {
            // 规范化文件夹路径
            folder = folder.replace("\\", "/");
            if (folder.startsWith("/")) {
                folder = folder.substring(1);
            }
            if (folder.endsWith("/")) {
                folder = folder.substring(0, folder.length() - 1);
            }
            return folder + "/" + fileName;
        }
        return fileName;
    }

    /**
     * 删除空的父目录（递归）
     */
    private void deleteEmptyParentDirs(Path dir) {
        try {
            Path basePath = Paths.get(localConfig.getFullBasePath());
            while (dir != null && !dir.equals(basePath)) {
                if (Files.isDirectory(dir) && isDirectoryEmpty(dir)) {
                    Files.delete(dir);
                    log.debug("删除空目录：{}", dir);
                    dir = dir.getParent();
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            log.warn("删除空目录失败：{}", e.getMessage());
        }
    }

    /**
     * 检查目录是否为空
     */
    private boolean isDirectoryEmpty(Path dir) throws IOException {
        try (Stream<Path> entries = Files.list(dir)) {
            return !entries.findFirst().isPresent();
        }
    }
}
