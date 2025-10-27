package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.linyi.cropseed.trace.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单关联实体
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_menu")
@Schema(description = "角色菜单关联")
public class SysRoleMenu extends BaseEntity {

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "菜单ID")
    private Long menuId;
}
