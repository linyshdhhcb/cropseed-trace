package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.dto.SeedCategoryAddDTO;
import com.linyi.cropseed.trace.dto.SeedCategoryUpdateDTO;
import com.linyi.cropseed.trace.entity.SeedCategory;
import com.linyi.cropseed.trace.service.SeedCategoryService;
import com.linyi.cropseed.trace.vo.SeedCategoryTreeVO;
import com.linyi.cropseed.trace.vo.SeedCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 种子品类Controller
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "种子品类管理")
@RestController
@RequestMapping("/seed/category")
@RequiredArgsConstructor
public class SeedCategoryController {

    private final SeedCategoryService seedCategoryService;

    @Operation(summary = "获取品类树形结构")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('seed:category:list')")
    public Result<List<SeedCategoryTreeVO>> getCategoryTree() {
        List<SeedCategoryTreeVO> tree = seedCategoryService.getCategoryTree();
        return Result.success(tree);
    }

    @Operation(summary = "查询品类详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('seed:category:query')")
    public Result<SeedCategory> getCategoryById(@Parameter(description = "品类ID") @PathVariable Long id) {
        SeedCategory category = seedCategoryService.getCategoryById(id);
        return Result.success(category);
    }

    @Operation(summary = "新增品类")
    @PostMapping
    @PreAuthorize("hasAuthority('seed:category:add')")
    public Result<Void> addCategory(@Valid @RequestBody SeedCategoryAddDTO categoryAddDTO) {
        SeedCategory category = new SeedCategory();
        category.setCategoryName(categoryAddDTO.getCategoryName());
        category.setCategoryCode(categoryAddDTO.getCategoryCode());
        category.setParentId(categoryAddDTO.getParentId());
        category.setDescription(categoryAddDTO.getDescription());
        category.setSort(categoryAddDTO.getSort());
        category.setStatus(categoryAddDTO.getStatus());

        seedCategoryService.addCategory(category);
        return Result.success("品类新增成功");
    }

    @Operation(summary = "修改品类")
    @PutMapping
    @PreAuthorize("hasAuthority('seed:category:edit')")
    public Result<Void> updateCategory(@Valid @RequestBody SeedCategoryUpdateDTO categoryUpdateDTO) {
        SeedCategory category = new SeedCategory();
        category.setId(categoryUpdateDTO.getId());
        category.setCategoryName(categoryUpdateDTO.getCategoryName());
        category.setCategoryCode(categoryUpdateDTO.getCategoryCode());
        category.setParentId(categoryUpdateDTO.getParentId());
        category.setDescription(categoryUpdateDTO.getDescription());
        category.setSort(categoryUpdateDTO.getSort());
        category.setStatus(categoryUpdateDTO.getStatus());

        seedCategoryService.updateCategory(category);
        return Result.success("品类修改成功");
    }

    @Operation(summary = "删除品类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('seed:category:remove')")
    public Result<Void> deleteCategory(@Parameter(description = "品类ID") @PathVariable Long id) {
        seedCategoryService.deleteCategory(id);
        return Result.success("品类删除成功");
    }

    @Operation(summary = "根据父ID查询子品类")
    @GetMapping("/parent/{parentId}")
    @PreAuthorize("hasAuthority('seed:category:list')")
    public Result<List<SeedCategory>> getCategoriesByParentId(
            @Parameter(description = "父品类ID") @PathVariable Long parentId) {
        List<SeedCategory> categories = seedCategoryService.getCategoriesByParentId(parentId);
        return Result.success(categories);
    }

    @Operation(summary = "获取品类列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('seed:category:list')")
    public Result<List<SeedCategoryVO>> getCategoryList(
            @Parameter(description = "品类名称") @RequestParam(required = false) String categoryName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        List<SeedCategoryVO> result = seedCategoryService.getCategoryList(categoryName, status);
        return Result.success(result);
    }

    @Operation(summary = "批量删除品类")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('seed:category:remove')")
    public Result<Void> batchDeleteCategories(@Parameter(description = "品类ID列表") @RequestBody List<Long> ids) {
        seedCategoryService.batchDeleteCategories(ids);
        return Result.success("批量删除成功");
    }
}
