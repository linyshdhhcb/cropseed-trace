package com.linyi.cropseed.trace.module.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.constant.CacheConstants;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import com.linyi.cropseed.trace.module.system.model.entity.SysMenu;
import com.linyi.cropseed.trace.module.system.model.entity.SysRole;
import com.linyi.cropseed.trace.module.system.model.entity.SysUser;
import com.linyi.cropseed.trace.module.system.model.entity.SysUserRole;
import com.linyi.cropseed.trace.module.system.mapper.SysMenuMapper;
import com.linyi.cropseed.trace.module.system.mapper.SysRoleMapper;
import com.linyi.cropseed.trace.module.system.mapper.SysUserMapper;
import com.linyi.cropseed.trace.module.system.mapper.SysUserRoleMapper;
import com.linyi.cropseed.trace.module.system.service.SysUserService;
import com.linyi.cropseed.trace.module.system.model.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户服务实现
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;

    @Override
    public PageResult<SysUserVO> pageUsers(PageQuery pageQuery, String username, String realName, Integer status) {
        Page<SysUser> page = pageQuery.toMpPageWithOrder();

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(username), SysUser::getUsername, username)
                .like(StrUtil.isNotBlank(realName), SysUser::getRealName, realName)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = this.page(page, wrapper);

        List<SysUserVO> userVOList = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(userPage, userVOList);
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_USER, key = "#id", unless = "#result == null")
    public SysUserVO getUserById(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConstants.CACHE_USER, key = "'username:' + #user.username")
    public void addUser(SysUser user, List<Long> roleIds) {
        // 检查用户名是否已存在
        if (getUserByUsername(user.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }

        // 加密密码
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));

        // 保存用户
        this.save(user);

        // 分配角色
        if (CollUtil.isNotEmpty(roleIds)) {
            assignRoles(user.getId(), roleIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.CACHE_USER, key = "#user.id"),
            @CacheEvict(value = CacheConstants.CACHE_USER, key = "'username:' + #user.username"),
            @CacheEvict(value = CacheConstants.CACHE_USER_ROLES, key = "#user.id"),
            @CacheEvict(value = CacheConstants.CACHE_USER_PERMISSIONS, key = "#user.id")
    })
    public void updateUser(SysUser user, List<Long> roleIds) {
        SysUser existUser = this.getById(user.getId());
        if (existUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 检查用户名是否被其他用户使用
        SysUser userByUsername = getUserByUsername(user.getUsername());
        if (userByUsername != null && !userByUsername.getId().equals(user.getId())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }

        // 如果密码不为空，则加密密码
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        } else {
            // 如果密码为空，则保持原密码
            user.setPassword(existUser.getPassword());
        }

        // 更新用户
        this.updateById(user);

        // 重新分配角色
        if (roleIds != null) {
            // 删除原有角色
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, user.getId()));

            // 分配新角色
            if (CollUtil.isNotEmpty(roleIds)) {
                assignRoles(user.getId(), roleIds);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.CACHE_USER, key = "#id"),
            @CacheEvict(value = CacheConstants.CACHE_USER_ROLES, key = "#id"),
            @CacheEvict(value = CacheConstants.CACHE_USER_PERMISSIONS, key = "#id")
    })
    public void deleteUser(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 删除用户角色关联
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));

        // 删除用户
        this.removeById(id);
    }

    @Override
    @CacheEvict(value = CacheConstants.CACHE_USER, key = "#id")
    public void resetPassword(Long id, String newPassword) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        user.setPassword(SecurityUtils.encryptPassword(newPassword));
        this.updateById(user);
    }

    @Override
    @CacheEvict(value = CacheConstants.CACHE_USER, key = "#id")
    public void changePassword(Long id, String oldPassword, String newPassword) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证旧密码
        if (!SecurityUtils.matchPassword(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        user.setPassword(SecurityUtils.encryptPassword(newPassword));
        this.updateById(user);
    }

    @Override
    // 暂时移除缓存，因为@JsonIgnore导致密码字段缓存丢失
    // @Cacheable(value = CacheConstants.CACHE_USER, key = "'username:' + #username", unless = "#result == null")
    public SysUser getUserByUsername(String username) {
        return baseMapper.getUserByUsername(username);
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_USER_ROLES, key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<String> getUserRoles(Long userId) {
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
        return roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_USER_PERMISSIONS, key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<String> getUserPermissions(Long userId) {
        // 实现权限查询逻辑
        // 先查询用户角色
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);

        // 检查是否为超级管理员
        boolean isSuperAdmin = roles.stream()
                .anyMatch(role -> "SUPER_ADMIN".equals(role.getRoleCode()));

        if (isSuperAdmin) {
            // 超级管理员拥有所有权限，返回所有菜单权限
            List<SysMenu> allMenus = sysMenuMapper.selectList(null);
            return allMenus.stream()
                    .filter(menu -> StringUtils.hasText(menu.getPermission()))
                    .map(SysMenu::getPermission)
                    .distinct()
                    .collect(Collectors.toList());
        }

        // 普通用户通过角色菜单关联查询权限
        List<SysMenu> menus = sysMenuMapper.selectMenusByUserId(userId);
        return menus.stream()
                .filter(menu -> StringUtils.hasText(menu.getPermission()))
                .map(SysMenu::getPermission)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.CACHE_USER_ROLES, key = "#userId"),
            @CacheEvict(value = CacheConstants.CACHE_USER_PERMISSIONS, key = "#userId")
    })
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));

        // 分配新角色
        if (CollUtil.isNotEmpty(roleIds)) {
            List<SysUserRole> userRoles = roleIds.stream()
                    .map(roleId -> {
                        SysUserRole userRole = new SysUserRole();
                        userRole.setUserId(userId);
                        userRole.setRoleId(roleId);
                        return userRole;
                    })
                    .collect(Collectors.toList());

            for (SysUserRole userRole : userRoles) {
                sysUserRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.CACHE_USER, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_USER_ROLES, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_USER_PERMISSIONS, allEntries = true)
    })
    public void batchDeleteUsers(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }

        // 删除用户角色关联
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysUserRole::getUserId, ids);
        sysUserRoleMapper.delete(wrapper);

        // 删除用户
        removeByIds(ids);
    }

    /**
     * 转换为VO对象
     */
    private SysUserVO convertToVO(SysUser user) {
        SysUserVO userVO = BeanUtil.copyProperties(user, SysUserVO.class);

        // 查询用户角色
        List<String> roles = getUserRoles(user.getId());
        userVO.setRoles(roles);

        return userVO;
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_USER, key = "'profile:' + #userId", unless = "#result == null")
    public SysUserVO getCurrentUserProfile(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return convertToVO(user);
    }

    @Override
    @CacheEvict(value = CacheConstants.CACHE_USER, key = "'profile:' + #userId")
    public void updateProfile(Long userId, String realName, String phone, String email, String avatar) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 只更新非空字段
        if (StrUtil.isNotBlank(realName)) {
            user.setRealName(realName);
        }
        if (StrUtil.isNotBlank(phone)) {
            user.setPhone(phone);
        }
        if (StrUtil.isNotBlank(email)) {
            user.setEmail(email);
        }
        if (StrUtil.isNotBlank(avatar)) {
            user.setAvatar(avatar);
        }

        this.updateById(user);
    }

    @Override
    @CacheEvict(value = CacheConstants.CACHE_USER, key = "'profile:' + #userId")
    public void updateAvatar(Long userId, String avatar) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        user.setAvatar(avatar);
        this.updateById(user);
    }
}
