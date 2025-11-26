package com.linyi.cropseed.trace.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linyi.cropseed.trace.common.constant.CacheConstants;
import com.linyi.cropseed.trace.module.system.model.entity.SysMenu;
import com.linyi.cropseed.trace.module.system.model.entity.SysRole;
import com.linyi.cropseed.trace.module.system.mapper.SysMenuMapper;
import com.linyi.cropseed.trace.module.system.mapper.SysRoleMapper;
import com.linyi.cropseed.trace.module.system.service.SysMenuService;
import com.linyi.cropseed.trace.module.system.model.vo.SysMenuVO;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统菜单Service实现类
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<SysMenuVO> getMenuList(String menuName, Integer status) {
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(menuName), SysMenu::getMenuName, menuName)
                .eq(status != null, SysMenu::getStatus, status)
                .orderByAsc(SysMenu::getSort);

        List<SysMenu> menus = sysMenuMapper.selectList(queryWrapper);
        return menus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_MENU_TREE, unless = "#result == null || #result.isEmpty()")
    public List<SysMenuVO> getMenuTree() {
        // 实现菜单树形结构
        List<SysMenu> allMenus = sysMenuMapper.selectList(null);
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_MENU, key = "#id", unless = "#result == null")
    public SysMenuVO getMenuById(Long id) {
        SysMenu menu = sysMenuMapper.selectById(id);
        return menu != null ? convertToVO(menu) : null;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.CACHE_MENU_TREE, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_MENU_LIST, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_USER_PERMISSIONS, allEntries = true)
    })
    public void addMenu(SysMenu menu) {
        sysMenuMapper.insert(menu);
        log.info("新增菜单成功: {}", menu.getMenuName());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.CACHE_MENU, key = "#menu.id"),
            @CacheEvict(value = CacheConstants.CACHE_MENU_TREE, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_MENU_LIST, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_USER_PERMISSIONS, allEntries = true)
    })
    public void updateMenu(SysMenu menu) {
        sysMenuMapper.updateById(menu);
        log.info("修改菜单成功: {}", menu.getMenuName());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.CACHE_MENU, key = "#id"),
            @CacheEvict(value = CacheConstants.CACHE_MENU_TREE, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_MENU_LIST, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_ROLE_MENUS, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_USER_PERMISSIONS, allEntries = true)
    })
    public void deleteMenu(Long id) {
        sysMenuMapper.deleteById(id);
        log.info("删除菜单成功: {}", id);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.CACHE_MENU, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_MENU_TREE, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_MENU_LIST, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_ROLE_MENUS, allEntries = true),
            @CacheEvict(value = CacheConstants.CACHE_USER_PERMISSIONS, allEntries = true)
    })
    public void batchDeleteMenus(List<Long> ids) {
        sysMenuMapper.deleteBatchIds(ids);
        log.info("批量删除菜单成功: {}", ids);
    }

    private SysMenuVO convertToVO(SysMenu menu) {
        SysMenuVO vo = new SysMenuVO();
        BeanUtils.copyProperties(menu, vo);
        // 手动映射permission字段到perms
        vo.setPerms(menu.getPermission());
        return vo;
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_USER_ROUTES, 
               key = "T(com.linyi.cropseed.trace.common.util.SecurityUtils).getCurrentUserId()", 
               unless = "#result == null || #result.isEmpty()")
    public List<SysMenuVO> getUserRouterMenu() {
        // 获取当前登录用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 查询用户的角色列表
        List<SysRole> userRoles = sysRoleMapper.selectRolesByUserId(userId);
        
        // 判断是否是超级管理员（role_id=1 或 role_code='SUPER_ADMIN'）
        boolean isSuperAdmin = userRoles.stream()
                .anyMatch(role -> role.getId().equals(1L) || "SUPER_ADMIN".equals(role.getRoleCode()));
        
        List<SysMenu> userMenus;
        if (isSuperAdmin) {
            // 超级管理员：查询所有菜单
            log.info("用户{}是超级管理员，返回所有菜单", userId);
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysMenu::getDeletedFlag, 0)
                    .eq(SysMenu::getStatus, 1)
                    .orderByAsc(SysMenu::getSort);
            userMenus = sysMenuMapper.selectList(queryWrapper);
        } else {
            // 普通用户：根据角色关联查询菜单
            userMenus = sysMenuMapper.selectMenusByUserId(userId);
        }
        
        // 过滤菜单：只保留目录(1)和菜单(2)类型，排除按钮(3)
        List<SysMenu> routerMenus = userMenus.stream()
                .filter(menu -> menu.getMenuType() == 1 || menu.getMenuType() == 2)
                .filter(menu -> menu.getStatus() == 1) // 只保留启用的菜单
                .collect(Collectors.toList());
        
        // 构建树形结构
        return buildMenuTree(routerMenus, 0L);
    }

    // 构建菜单树
    private List<SysMenuVO> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> Objects.equals(menu.getParentId(), parentId))
                .map(menu -> {
                    SysMenuVO vo = convertToVO(menu);
                    List<SysMenuVO> children = buildMenuTree(menus, menu.getId());
                    vo.setChildren(children);
                    return vo;
                })
                .sorted(Comparator.comparing(SysMenuVO::getSort))
                .collect(Collectors.toList());
    }
}
