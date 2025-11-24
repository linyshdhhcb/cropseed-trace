package com.linyi.cropseed.trace.module.inventory.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.inventory.model.dto.InventoryOutboundDTO;
import com.linyi.cropseed.trace.module.inventory.service.InventoryOutboundService;
import com.linyi.cropseed.trace.module.inventory.model.vo.InventoryOutboundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 出库管理Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "出库管理")
@RestController
@RequestMapping("/inventory/outbound")
@RequiredArgsConstructor
public class InventoryOutboundController {

    private final InventoryOutboundService inventoryOutboundService;

    @Operation(summary = "分页查询出库记录列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('inventory:outbound:list')")
    public Result<PageResult<InventoryOutboundVO>> pageOutboundRecords(PageQuery pageQuery,
            @Parameter(description = "批次号") @RequestParam(required = false) String batchNo,
            @Parameter(description = "仓库ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageResult<InventoryOutboundVO> result = inventoryOutboundService.pageOutboundRecords(pageQuery, batchNo,
                warehouseId, status);
        return Result.success(result);
    }

    @Operation(summary = "查询出库记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:outbound:query')")
    public Result<InventoryOutboundVO> getOutboundRecordById(@Parameter(description = "出库记录ID") @PathVariable Long id) {
        InventoryOutboundVO record = inventoryOutboundService.getOutboundRecordById(id);
        return Result.success(record);
    }

    @Operation(summary = "新增出库记录")
    @PostMapping
    @PreAuthorize("hasAuthority('inventory:outbound:add')")
    public Result<Void> addOutboundRecord(@Valid @RequestBody InventoryOutboundDTO outboundDTO) {
        inventoryOutboundService.addOutboundRecord(outboundDTO);
        return Result.success("出库记录新增成功");
    }

    @Operation(summary = "修改出库记录")
    @PutMapping
    @PreAuthorize("hasAuthority('inventory:outbound:edit')")
    public Result<Void> updateOutboundRecord(@Valid @RequestBody InventoryOutboundDTO outboundDTO) {
        inventoryOutboundService.updateOutboundRecord(outboundDTO);
        return Result.success("出库记录修改成功");
    }

    @Operation(summary = "删除出库记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:outbound:remove')")
    public Result<Void> deleteOutboundRecord(@Parameter(description = "出库记录ID") @PathVariable Long id) {
        inventoryOutboundService.deleteOutboundRecord(id);
        return Result.success("出库记录删除成功");
    }

    @Operation(summary = "批量删除出库记录")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('inventory:outbound:remove')")
    public Result<Void> batchDeleteOutboundRecords(@Parameter(description = "出库记录ID列表") @RequestBody List<Long> ids) {
        inventoryOutboundService.batchDeleteOutboundRecords(ids);
        return Result.success("批量删除成功");
    }

    @Operation(summary = "审批出库单")
    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('inventory:outbound:edit')")
    public Result<Void> approve(@RequestParam Long id,
            @Parameter(description = "是否通过，true-通过，false-不通过") @RequestParam Boolean approved,
            @Parameter(description = "审批备注") @RequestParam(required = false) String remark) {
        inventoryOutboundService.approve(id, approved, remark);
        return Result.success(approved ? "审批通过" : "审批不通过");
    }

    @Operation(summary = "确认出库单")
    @PostMapping("/confirm")
    @PreAuthorize("hasAuthority('inventory:outbound:edit')")
    public Result<Void> confirm(@RequestParam Long id,
            @RequestParam(required = false) String remark) {
        inventoryOutboundService.confirm(id, remark);
        return Result.success("确认成功");
    }

    @Operation(summary = "取消出库单")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('inventory:outbound:edit')")
    public Result<Void> cancel(@RequestParam Long id,
            @RequestParam(required = false) String reason) {
        inventoryOutboundService.cancel(id, reason);
        return Result.success("取消成功");
    }
}
