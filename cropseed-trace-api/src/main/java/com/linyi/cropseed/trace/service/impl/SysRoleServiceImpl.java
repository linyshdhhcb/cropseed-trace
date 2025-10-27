package com.linyi.cropseed.trace.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.entity.SysRole;
import com.linyi.cropseed.trace.entity.SysRoleMenu;
import com.linyi.cropseed.trace.mapper.SysRoleMapper;
import com.linyi.cropseed.trace.mapper.SysRoleMenuMapper;
import com.linyi.cropseed.trace.service.SysRoleService;
import com.linyi.cropseed.trace.vo.SysRoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统角色Service实现类
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public PageResult<SysRoleVO> pageRoles(PageQuery pageQuery, String roleName, String roleCode, Integer status) {
        Page<SysRole> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .like(StringUtils.hasText(roleCode), SysRole::getRoleCode, roleCode)
                .eq(status != null, SysRole::getStatus, status)
                .orderByDesc(SysRole::getId);

        Page<SysRole> result = sysRoleMapper.selectPage(page, queryWrapper);

        List<SysRoleVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result, voList);
    }

    @Override
    public SysRoleVO getRoleById(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        return role != null ? convertToVO(role) : null;
    }

    @Override
    @Transactional
    public void addRole(SysRole role, List<Long> menuIds) {
        sysRoleMapper.insert(role);
        // 分配菜单权限
        if (CollUtil.isNotEmpty(menuIds)) {
            assignMenus(role.getId(), menuIds);
        }
        log.info("新增角色成功: {}", role.getRoleName());
    }

    @Override
    @Transactional
    public void updateRole(SysRole role, List<Long> menuIds) {
        sysRoleMapper.updateById(role);
        // 更新菜单权限
        if (CollUtil.isNotEmpty(menuIds)) {
            assignMenus(role.getId(), menuIds);
        }
        log.info("修改角色成功: {}", role.getRoleName());
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        // 检查是否为超级管理员角色，不允许删除
        SysRole role = sysRoleMapper.selectById(id);
        if (role != null && "SUPER_ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException(ResultCode.ROLE_CANNOT_DELETE, "超级管理员角色不允许删除");
        }

        sysRoleMapper.deleteById(id);
        // 删除角色权限关联
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, id);
        sysRoleMenuMapper.delete(wrapper);
        log.info("删除角色成功: {}", id);
    }

    @Override
    @Transactional
    public void batchDeleteRoles(List<Long> ids) {
        // 检查是否包含超级管理员角色，不允许删除
        List<SysRole> roles = sysRoleMapper.selectBatchIds(ids);
        boolean hasSuperAdmin = roles.stream()
                .anyMatch(role -> "SUPER_ADMIN".equals(role.getRoleCode()));
        if (hasSuperAdmin) {
            throw new BusinessException(ResultCode.ROLE_CANNOT_DELETE, "超级管理员角色不允许删除");
        }

        sysRoleMapper.deleteBatchIds(ids);
        // 批量删除角色权限关联
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysRoleMenu::getRoleId, ids);
        sysRoleMenuMapper.delete(wrapper);
        log.info("批量删除角色成功: {}", ids);
    }

    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 先删除原有权限
        LambdaQueryWrapper<SysRoleMenu> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysRoleMenu::getRoleId, roleId);
        sysRoleMenuMapper.delete(deleteWrapper);

        // 添加新权限
        if (CollUtil.isNotEmpty(menuIds)) {
            List<SysRoleMenu> roleMenus = menuIds.stream()
                    .map(menuId -> {
                        SysRoleMenu roleMenu = new SysRoleMenu();
                        roleMenu.setRoleId(roleId);
                        roleMenu.setMenuId(menuId);
                        return roleMenu;
                    })
                    .collect(Collectors.toList());
            for (SysRoleMenu roleMenu : roleMenus) {
                sysRoleMenuMapper.insert(roleMenu);
            }
        }
        log.info("分配菜单权限成功: roleId={}, menuIds={}", roleId, menuIds);
    }

    @Override
    public List<Long> getRoleMenus(Long roleId) {
        // 获取角色菜单权限
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(wrapper);
        return roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    private SysRoleVO convertToVO(SysRole role) {
        SysRoleVO vo = new SysRoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
