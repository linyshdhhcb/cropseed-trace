package com.linyi.cropseed.trace.module.system.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.system.model.dto.SysMenuAddDTO;
import com.linyi.cropseed.trace.module.system.model.dto.SysMenuUpdateDTO;
import com.linyi.cropseed.trace.module.system.model.entity.SysMenu;
import com.linyi.cropseed.trace.module.system.service.SysMenuService;
import com.linyi.cropseed.trace.module.system.model.vo.SysMenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统菜单Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "系统菜单管理")
@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @Operation(summary = "获取菜单列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<SysMenuVO>> getMenuList(
            @Parameter(description = "菜单名称") @RequestParam(required = false) String menuName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        List<SysMenuVO> result = sysMenuService.getMenuList(menuName, status);
        return Result.success(result);
    }

    @Operation(summary = "获取菜单树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<SysMenuVO>> getMenuTree() {
        List<SysMenuVO> result = sysMenuService.getMenuTree();
        return Result.success(result);
    }

    @Operation(summary = "查询菜单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public Result<SysMenuVO> getMenuById(@Parameter(description = "菜单ID") @PathVariable Long id) {
        SysMenuVO menu = sysMenuService.getMenuById(id);
        return Result.success(menu);
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<Void> addMenu(@Valid @RequestBody SysMenuAddDTO menuAddDTO) {
        SysMenu menu = new SysMenu();
        menu.setMenuName(menuAddDTO.getMenuName());
        menu.setParentId(menuAddDTO.getParentId());
        menu.setPath(menuAddDTO.getPath());
        menu.setComponent(menuAddDTO.getComponent());
        menu.setMenuType(menuAddDTO.getMenuType());
        menu.setIcon(menuAddDTO.getIcon());
        menu.setSort(menuAddDTO.getSort());
        menu.setStatus(menuAddDTO.getStatus());

        sysMenuService.addMenu(menu);
        return Result.success("菜单新增成功");
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> updateMenu(@Valid @RequestBody SysMenuUpdateDTO menuUpdateDTO) {
        SysMenu menu = new SysMenu();
        menu.setId(menuUpdateDTO.getId());
        menu.setMenuName(menuUpdateDTO.getMenuName());
        menu.setParentId(menuUpdateDTO.getParentId());
        menu.setPath(menuUpdateDTO.getPath());
        menu.setComponent(menuUpdateDTO.getComponent());
        menu.setMenuType(menuUpdateDTO.getMenuType());
        menu.setIcon(menuUpdateDTO.getIcon());
        menu.setSort(menuUpdateDTO.getSort());
        menu.setStatus(menuUpdateDTO.getStatus());

        sysMenuService.updateMenu(menu);
        return Result.success("菜单修改成功");
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    public Result<Void> deleteMenu(@Parameter(description = "菜单ID") @PathVariable Long id) {
        sysMenuService.deleteMenu(id);
        return Result.success("菜单删除成功");
    }

    @Operation(summary = "批量删除菜单")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:menu:remove')")
    public Result<Void> batchDeleteMenus(@Parameter(description = "菜单ID列表") @RequestBody List<Long> ids) {
        sysMenuService.batchDeleteMenus(ids);
        return Result.success("批量删除成功");
    }
}
