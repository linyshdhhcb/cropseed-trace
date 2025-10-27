package com.linyi.cropseed.trace.service;

import com.linyi.cropseed.trace.entity.SysMenu;
import com.linyi.cropseed.trace.vo.SysMenuVO;

import java.util.List;

/**
 * 系统菜单Service接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface SysMenuService {

    /**
     * 获取菜单列表
     */
    List<SysMenuVO> getMenuList(String menuName, Integer status);

    /**
     * 获取菜单树
     */
    List<SysMenuVO> getMenuTree();

    /**
     * 根据ID查询菜单详情
     */
    SysMenuVO getMenuById(Long id);

    /**
     * 新增菜单
     */
    void addMenu(SysMenu menu);

    /**
     * 修改菜单
     */
    void updateMenu(SysMenu menu);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);

    /**
     * 批量删除菜单
     */
    void batchDeleteMenus(List<Long> ids);
}
