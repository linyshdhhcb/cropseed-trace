package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.infrastructure.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件管理Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        // 验证文件大小（100MB）
        if (file.getSize() > 100 * 1024 * 1024) {
            return Result.fail("文件大小不能超过100MB");
        }

        String url = storageService.uploadFile(file, folder);
        return Result.success("文件上传成功", url);
    }

    @Operation(summary = "批量上传文件")
    @PostMapping("/upload/batch")
    public Result<java.util.List<String>> uploadBatch(@RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", required = false) String folder) {
        if (files == null || files.length == 0) {
            return Result.fail("文件不能为空");
        }

        java.util.List<String> urls = new java.util.ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String url = storageService.uploadFile(file, folder);
                urls.add(url);
            }
        }

        return Result.success("文件批量上传成功", urls);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download")
    public void download(@RequestParam("objectName") String objectName,
            HttpServletResponse response) {
        try {
            InputStream inputStream = storageService.downloadFile(objectName);

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode(objectName, StandardCharsets.UTF_8));

            // 输出文件
            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            inputStream.close();
        } catch (Exception e) {
            log.error("文件下载失败：{}", e.getMessage());
        }
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam("objectName") String objectName) {
        storageService.deleteFile(objectName);
        return Result.success("文件删除成功");
    }

    @Operation(summary = "获取文件预签名URL")
    @GetMapping("/presigned-url")
    public Result<String> getPresignedUrl(@RequestParam("objectName") String objectName,
            @RequestParam(value = "expiry", defaultValue = "60") int expiry) {
        String url = storageService.getPresignedUrl(objectName, expiry);
        return Result.success("获取预签名URL成功", url);
    }

    @Operation(summary = "获取文件列表")
    @GetMapping("/list")
    public Result<java.util.List<String>> list(@RequestParam(value = "prefix", required = false) String prefix) {
        java.util.List<String> files = storageService.listFiles(prefix);
        return Result.success("获取文件列表成功", files);
    }
}
