package com.linyi.cropseed.trace.module.inventory.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.entity.Warehouse;
import com.linyi.cropseed.trace.module.inventory.service.WarehouseService;
import com.linyi.cropseed.trace.module.inventory.model.vo.WarehouseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库管理Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "仓库管理")
@RestController
@RequestMapping("/inventory/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Operation(summary = "分页查询仓库列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('warehouse:list')")
    public Result<PageResult<WarehouseVO>> pageWarehouses(PageQuery pageQuery,
            @Parameter(description = "仓库名称") @RequestParam(required = false) String warehouseName,
            @Parameter(description = "仓库编码") @RequestParam(required = false) String warehouseCode,
            @Parameter(description = "负责人") @RequestParam(required = false) String manager,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageResult<WarehouseVO> result = warehouseService.pageWarehouses(pageQuery, warehouseName, warehouseCode,
                manager, status);
        return Result.success(result);
    }

    @Operation(summary = "查询所有启用的仓库")
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('warehouse:list')")
    public Result<List<WarehouseVO>> getAllActiveWarehouses() {
        List<WarehouseVO> warehouses = warehouseService.getAllActiveWarehouses();
        return Result.success(warehouses);
    }

    @Operation(summary = "查询仓库详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:query')")
    public Result<WarehouseVO> getWarehouseById(@Parameter(description = "仓库ID") @PathVariable Long id) {
        WarehouseVO warehouse = warehouseService.getWarehouseById(id);
        return Result.success(warehouse);
    }

    @Operation(summary = "新增仓库")
    @PostMapping
    @PreAuthorize("hasAuthority('warehouse:add')")
    public Result<Void> addWarehouse(@Valid @RequestBody Warehouse warehouse) {
        warehouseService.addWarehouse(warehouse);
        return Result.success("仓库新增成功");
    }

    @Operation(summary = "修改仓库")
    @PutMapping
    @PreAuthorize("hasAuthority('warehouse:edit')")
    public Result<Void> updateWarehouse(@Valid @RequestBody Warehouse warehouse) {
        warehouseService.updateWarehouse(warehouse);
        return Result.success("仓库修改成功");
    }

    @Operation(summary = "删除仓库")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('warehouse:remove')")
    public Result<Void> deleteWarehouse(@Parameter(description = "仓库ID") @PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return Result.success("仓库删除成功");
    }

    @Operation(summary = "批量删除仓库")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('warehouse:remove')")
    public Result<Void> batchDeleteWarehouses(@Parameter(description = "仓库ID列表") @RequestBody List<Long> ids) {
        warehouseService.batchDeleteWarehouses(ids);
        return Result.success("批量删除成功");
    }
}
