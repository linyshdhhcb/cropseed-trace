package com.linyi.cropseed.trace.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.entity.SysUser;
import com.linyi.cropseed.trace.vo.SysUserVO;

import java.util.List;

/**
 * 系统用户服务接口
 * 
 * @author LinYi
 * @since 2025-10-25
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     */
    PageResult<SysUserVO> pageUsers(PageQuery pageQuery, String username, String realName, Integer status);

    /**
     * 根据ID查询用户详情
     */
    SysUserVO getUserById(Long id);

    /**
     * 新增用户
     */
    void addUser(SysUser user, List<Long> roleIds);

    /**
     * 修改用户
     */
    void updateUser(SysUser user, List<Long> roleIds);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 修改密码
     */
    void changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 根据用户名查询用户
     */
    SysUser getUserByUsername(String username);

    /**
     * 根据用户ID查询角色列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 根据用户ID查询权限列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 分配角色
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 批量删除用户
     */
    void batchDeleteUsers(List<Long> ids);
}
