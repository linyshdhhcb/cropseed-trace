package com.linyi.cropseed.trace.module.system.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 系统角色新增DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SysRoleAddDTO {
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private String description;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer sort;

    private List<Long> menuIds;
}
