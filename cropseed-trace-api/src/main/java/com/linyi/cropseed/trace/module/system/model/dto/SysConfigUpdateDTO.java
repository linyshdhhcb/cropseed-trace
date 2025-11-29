package com.linyi.cropseed.trace.module.system.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 系统配置更新DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SysConfigUpdateDTO {
    
    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;

    @NotBlank(message = "配置名称不能为空")
    @Size(max = 100, message = "配置名称长度不能超过100个字符")
    private String configName;

    private Integer configType;

    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;
}
