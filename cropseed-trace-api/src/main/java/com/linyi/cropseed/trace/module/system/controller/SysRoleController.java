package com.linyi.cropseed.trace.module.system.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.system.model.dto.SysRoleAddDTO;
import com.linyi.cropseed.trace.module.system.model.dto.SysRoleUpdateDTO;
import com.linyi.cropseed.trace.module.system.model.entity.SysRole;
import com.linyi.cropseed.trace.module.system.service.SysRoleService;
import com.linyi.cropseed.trace.module.system.model.vo.SysRoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "系统角色管理")
@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @Operation(summary = "分页查询角色列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<PageResult<SysRoleVO>> pageRoles(PageQuery pageQuery,
            @Parameter(description = "角色名称") @RequestParam(required = false) String roleName,
            @Parameter(description = "角色编码") @RequestParam(required = false) String roleCode,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageResult<SysRoleVO> result = sysRoleService.pageRoles(pageQuery, roleName, roleCode, status);
        return Result.success(result);
    }

    @Operation(summary = "查询角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<SysRoleVO> getRoleById(@Parameter(description = "角色ID") @PathVariable Long id) {
        SysRoleVO role = sysRoleService.getRoleById(id);
        return Result.success(role);
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<Void> addRole(@Valid @RequestBody SysRoleAddDTO roleAddDTO) {
        SysRole role = new SysRole();
        role.setRoleName(roleAddDTO.getRoleName());
        role.setRoleCode(roleAddDTO.getRoleCode());
        role.setDescription(roleAddDTO.getDescription());
        role.setStatus(roleAddDTO.getStatus());

        sysRoleService.addRole(role, roleAddDTO.getMenuIds());
        return Result.success("角色新增成功");
    }

    @Operation(summary = "修改角色")
    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> updateRole(@Valid @RequestBody SysRoleUpdateDTO roleUpdateDTO) {
        SysRole role = new SysRole();
        role.setId(roleUpdateDTO.getId());
        role.setRoleName(roleUpdateDTO.getRoleName());
        role.setRoleCode(roleUpdateDTO.getRoleCode());
        role.setDescription(roleUpdateDTO.getDescription());
        role.setStatus(roleUpdateDTO.getStatus());

        sysRoleService.updateRole(role, roleUpdateDTO.getMenuIds());
        return Result.success("角色修改成功");
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:remove')")
    public Result<Void> deleteRole(@Parameter(description = "角色ID") @PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success("角色删除成功");
    }

    @Operation(summary = "批量删除角色")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:role:remove')")
    public Result<Void> batchDeleteRoles(@Parameter(description = "角色ID列表") @RequestBody List<Long> ids) {
        sysRoleService.batchDeleteRoles(ids);
        return Result.success("批量删除成功");
    }

    @Operation(summary = "分配菜单")
    @PostMapping("/assign-menu")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> assignMenu(@Parameter(description = "角色ID") @RequestParam Long roleId,
            @Parameter(description = "菜单ID列表") @RequestBody List<Long> menuIds) {
        sysRoleService.assignMenus(roleId, menuIds);
        return Result.success("菜单分配成功");
    }

    @Operation(summary = "获取角色菜单")
    @GetMapping("/{roleId}/menus")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<List<Long>> getRoleMenus(@Parameter(description = "角色ID") @PathVariable Long roleId) {
        List<Long> menuIds = sysRoleService.getRoleMenus(roleId);
        return Result.success(menuIds);
    }

    @Operation(summary = "获取所有角色列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<SysRoleVO>> getAllRoles() {
        List<SysRoleVO> roles = sysRoleService.getAllRoles();
        return Result.success(roles);
    }
}
