package com.linyi.cropseed.trace.module.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统配置VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SysConfigVO {
    private Long id;
    private String configKey;
    private String configValue;
    private String configName;
    private Integer configType;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
