package com.linyi.cropseed.trace.service;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.entity.SysRole;
import com.linyi.cropseed.trace.vo.SysRoleVO;

import java.util.List;

/**
 * 系统角色Service接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface SysRoleService {

    /**
     * 分页查询角色列表
     */
    PageResult<SysRoleVO> pageRoles(PageQuery pageQuery, String roleName, String roleCode, Integer status);

    /**
     * 根据ID查询角色详情
     */
    SysRoleVO getRoleById(Long id);

    /**
     * 新增角色
     */
    void addRole(SysRole role, List<Long> menuIds);

    /**
     * 修改角色
     */
    void updateRole(SysRole role, List<Long> menuIds);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 批量删除角色
     */
    void batchDeleteRoles(List<Long> ids);

    /**
     * 分配菜单
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取角色菜单
     */
    List<Long> getRoleMenus(Long roleId);

    /**
     * 获取所有角色列表
     */
    List<SysRoleVO> getAllRoles();
}
