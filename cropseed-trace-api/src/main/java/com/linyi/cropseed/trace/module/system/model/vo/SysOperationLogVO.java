package com.linyi.cropseed.trace.module.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志VO
 *
 * @author LinYi
 * @since 2025-11-28
 */
@Data
@Schema(description = "操作日志VO")
public class SysOperationLogVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "操作人ID")
    private Long userId;

    @Schema(description = "操作人用户名")
    private String username;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作内容")
    private String content;

    @Schema(description = "请求路径")
    private String url;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求参数")
    private String param;

    @Schema(description = "IP地址")
    private String ip;

    @Schema(description = "IP地区")
    private String ipRegion;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "执行时间(毫秒)")
    private Long executeTime;

    @Schema(description = "操作状态：0-失败，1-成功")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
