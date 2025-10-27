package com.linyi.cropseed.trace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linyi.cropseed.trace.entity.SysMenu;
import com.linyi.cropseed.trace.mapper.SysMenuMapper;
import com.linyi.cropseed.trace.service.SysMenuService;
import com.linyi.cropseed.trace.vo.SysMenuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
    public List<SysMenuVO> getMenuTree() {
        // 实现菜单树形结构
        List<SysMenu> allMenus = sysMenuMapper.selectList(null);
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public SysMenuVO getMenuById(Long id) {
        SysMenu menu = sysMenuMapper.selectById(id);
        return menu != null ? convertToVO(menu) : null;
    }

    @Override
    @Transactional
    public void addMenu(SysMenu menu) {
        sysMenuMapper.insert(menu);
        log.info("新增菜单成功: {}", menu.getMenuName());
    }

    @Override
    @Transactional
    public void updateMenu(SysMenu menu) {
        sysMenuMapper.updateById(menu);
        log.info("修改菜单成功: {}", menu.getMenuName());
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        sysMenuMapper.deleteById(id);
        log.info("删除菜单成功: {}", id);
    }

    @Override
    @Transactional
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
