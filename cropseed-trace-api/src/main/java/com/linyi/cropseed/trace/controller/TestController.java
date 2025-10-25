package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试Controller
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "系统测试")
@RestController
@RequestMapping("/test")
public class TestController {

    @Operation(summary = "系统健康检查")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("message", "农作物种质资源数字化溯源系统运行正常");

        return Result.success("系统运行正常", data);
    }

    @Operation(summary = "获取系统信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getSystemInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("application", "农作物种质资源数字化溯源系统");
        data.put("version", "1.0.0");
        data.put("author", "LinYi");
        data.put("description", "集种子档案管理、库存仓储、订单交易与微信小程序商城于一体的数字化溯源系统");

        return Result.success("获取系统信息成功", data);
    }
}
