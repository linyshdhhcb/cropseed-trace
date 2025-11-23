package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.config.properties.LocalProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件访问控制器
 * 仅在使用本地存储时启用
 *
 * @author LinYi
 * @since 2025-11-23
 */
@Slf4j
@RestController
@RequestMapping("/storage/files")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class FileAccessController {

    private final LocalProperties localConfig;

    /**
     * 访问本地存储的文件
     * URL格式：/storage/files/{bucket}/{path}
     *
     * @param bucket 存储桶名称
     * @return 文件资源
     */
    @GetMapping("/{bucket}/**")
    public ResponseEntity<Resource> accessFile(
            @PathVariable String bucket,
            jakarta.servlet.http.HttpServletRequest request) {

        try {
            // 提取完整的文件路径
            String requestPath = request.getRequestURI();
            String prefix = "/storage/files/" + bucket + "/";
            String filePath = requestPath.substring(requestPath.indexOf(prefix) + prefix.length());

            // 构建完整路径
            Path fullPath = Paths.get(localConfig.getBasePath(), bucket, filePath);

            // 安全检查：防止路径遍历攻击
            Path basePath = Paths.get(localConfig.getBasePath(), bucket);
            if (!fullPath.normalize().startsWith(basePath.normalize())) {
                log.warn("检测到路径遍历攻击尝试：{}", filePath);
                return ResponseEntity.badRequest().build();
            }

            // 检查文件是否存在
            if (!Files.exists(fullPath) || !Files.isRegularFile(fullPath)) {
                log.warn("文件不存在：{}", fullPath);
                return ResponseEntity.notFound().build();
            }

            // 读取文件
            Resource resource = new FileSystemResource(fullPath);

            // 检测文件类型
            String contentType = Files.probeContentType(fullPath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));

            // 对于图片等资源，设置inline；对于其他文件，设置attachment
            if (contentType.startsWith("image/") || contentType.startsWith("video/") ||
                contentType.equals("application/pdf")) {
                headers.setContentDisposition(
                    org.springframework.http.ContentDisposition.inline()
                        .filename(fullPath.getFileName().toString())
                        .build()
                );
            } else {
                headers.setContentDisposition(
                    org.springframework.http.ContentDisposition.attachment()
                        .filename(fullPath.getFileName().toString())
                        .build()
                );
            }

            // 设置缓存
            headers.setCacheControl("max-age=31536000, public");

            log.debug("访问本地文件：{}", fullPath);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException e) {
            log.error("访问本地文件失败", e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("处理文件访问请求失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
