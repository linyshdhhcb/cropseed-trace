package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.dto.ChangePasswordDTO;
import com.linyi.cropseed.trace.dto.SysUserAddDTO;
import com.linyi.cropseed.trace.dto.SysUserUpdateDTO;
import com.linyi.cropseed.trace.entity.SysUser;
import com.linyi.cropseed.trace.service.SysUserService;
import com.linyi.cropseed.trace.vo.SysUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "系统用户管理")
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageResult<SysUserVO>> pageUsers(PageQuery pageQuery,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageResult<SysUserVO> result = sysUserService.pageUsers(pageQuery, username, realName, status);
        return Result.success(result);
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<SysUserVO> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        SysUserVO user = sysUserService.getUserById(id);
        return Result.success(user);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<Void> addUser(@Valid @RequestBody SysUserAddDTO userAddDTO) {
        SysUser user = new SysUser();
        user.setUsername(userAddDTO.getUsername());
        user.setPassword(userAddDTO.getPassword());
        user.setRealName(userAddDTO.getRealName());
        user.setPhone(userAddDTO.getPhone());
        user.setEmail(userAddDTO.getEmail());
        user.setAvatar(userAddDTO.getAvatar());
        user.setStatus(userAddDTO.getStatus());

        sysUserService.addUser(user, userAddDTO.getRoleIds());
        return Result.success("用户新增成功");
    }

    @Operation(summary = "修改用户")
    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> updateUser(@Valid @RequestBody SysUserUpdateDTO userUpdateDTO) {
        SysUser user = new SysUser();
        user.setId(userUpdateDTO.getId());
        user.setUsername(userUpdateDTO.getUsername());
        user.setPassword(userUpdateDTO.getPassword());
        user.setRealName(userUpdateDTO.getRealName());
        user.setPhone(userUpdateDTO.getPhone());
        user.setEmail(userUpdateDTO.getEmail());
        user.setAvatar(userUpdateDTO.getAvatar());
        user.setStatus(userUpdateDTO.getStatus());

        sysUserService.updateUser(user, userUpdateDTO.getRoleIds());
        return Result.success("用户修改成功");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:remove')")
    public Result<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success("用户删除成功");
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    public Result<Void> resetPassword(@Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        sysUserService.resetPassword(id, newPassword);
        return Result.success("密码重置成功");
    }

    @Operation(summary = "修改密码")
    @PutMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        // 获取当前登录用户ID
        Long currentUserId = getCurrentUserId();
        sysUserService.changePassword(currentUserId, changePasswordDTO.getOldPassword(),
                changePasswordDTO.getNewPassword());
        return Result.success("密码修改成功");
    }

    @Operation(summary = "分配角色")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> assignRoles(@Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "角色ID列表") @RequestBody List<Long> roleIds) {
        sysUserService.assignRoles(id, roleIds);
        return Result.success("角色分配成功");
    }

    @Operation(summary = "获取用户角色")
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<List<String>> getUserRoles(@Parameter(description = "用户ID") @PathVariable Long id) {
        List<String> roles = sysUserService.getUserRoles(id);
        return Result.success(roles);
    }

    @Operation(summary = "获取用户权限")
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<List<String>> getUserPermissions(@Parameter(description = "用户ID") @PathVariable Long id) {
        List<String> permissions = sysUserService.getUserPermissions(id);
        return Result.success(permissions);
    }

    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:user:remove')")
    public Result<Void> batchDeleteUsers(@Parameter(description = "用户ID列表") @RequestBody List<Long> ids) {
        sysUserService.batchDeleteUsers(ids);
        return Result.success("批量删除成功");
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }
}
