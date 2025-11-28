package com.linyi.cropseed.trace.module.system.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志查询DTO
 *
 * @author LinYi
 * @since 2025-11-28
 */
@Data
@Schema(description = "操作日志查询DTO")
public class SysOperationLogQueryDTO {

    @Schema(description = "操作人用户名")
    private String username;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作内容关键词")
    private String content;

    @Schema(description = "操作状态：0-失败，1-成功")
    private Integer status;

    @Schema(description = "IP地址")
    private String ip;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
