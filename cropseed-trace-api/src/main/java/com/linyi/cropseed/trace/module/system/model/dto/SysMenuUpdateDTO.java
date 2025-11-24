package com.linyi.cropseed.trace.module.system.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 系统菜单修改DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SysMenuUpdateDTO {
    @NotNull(message = "菜单ID不能为空")
    private Long id;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    private Long parentId;

    private String path;

    private String component;

    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;

    private String icon;

    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer visible;

    private String perms;
}
