package com.linyi.cropseed.trace.common.util;

import com.linyi.cropseed.trace.entity.SysMenu;
import com.linyi.cropseed.trace.entity.SysRole;
import com.linyi.cropseed.trace.entity.SysUser;
import com.linyi.cropseed.trace.mapper.SysRoleMapper;
import com.linyi.cropseed.trace.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限工具类
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Component
@RequiredArgsConstructor
public class PermissionUtils {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    /**
     * 检查当前用户是否为超级管理员
     */
    public boolean isSuperAdmin() {
        SysUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return isSuperAdmin(currentUser.getId());
    }

    /**
     * 检查指定用户是否为超级管理员
     */
    public boolean isSuperAdmin(Long userId) {
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
        return roles.stream()
                .anyMatch(role -> "SUPER_ADMIN".equals(role.getRoleCode()));
    }

    /**
     * 获取当前登录用户
     */
    public SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SysUser) {
            return (SysUser) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId() {
        SysUser user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 检查用户是否有指定权限
     * 超级管理员默认拥有所有权限
     */
    public boolean hasPermission(String permission) {
        SysUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // 超级管理员拥有所有权限
        if (isSuperAdmin(currentUser.getId())) {
            return true;
        }

        // 普通用户暂时返回false，后续完善
        return false;
    }
}
