package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.dto.InventoryInboundDTO;
import com.linyi.cropseed.trace.entity.InventoryInbound;
import com.linyi.cropseed.trace.service.InventoryInboundService;
import com.linyi.cropseed.trace.vo.InventoryInboundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 入库管理Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "入库管理")
@RestController
@RequestMapping("/inventory/inbound")
@RequiredArgsConstructor
public class InventoryInboundController {

    private final InventoryInboundService inventoryInboundService;

    @Operation(summary = "分页查询入库记录列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('inventory:inbound:list')")
    public Result<PageResult<InventoryInboundVO>> pageInboundRecords(PageQuery pageQuery,
            @Parameter(description = "批次号") @RequestParam(required = false) String batchNo,
            @Parameter(description = "仓库ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        PageResult<InventoryInboundVO> result = inventoryInboundService.pageInboundRecords(pageQuery, batchNo,
                warehouseId, status);
        return Result.success(result);
    }

    @Operation(summary = "查询入库记录详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:inbound:query')")
    public Result<InventoryInboundVO> getInboundRecordById(@Parameter(description = "入库记录ID") @PathVariable Long id) {
        InventoryInboundVO record = inventoryInboundService.getInboundRecordById(id);
        return Result.success(record);
    }

    @Operation(summary = "新增入库记录")
    @PostMapping
    @PreAuthorize("hasAuthority('inventory:inbound:add')")
    public Result<Void> addInboundRecord(@Valid @RequestBody InventoryInboundDTO inboundDTO) {
        inventoryInboundService.addInboundRecord(inboundDTO);
        return Result.success("入库记录新增成功");
    }

    @Operation(summary = "修改入库记录")
    @PutMapping
    @PreAuthorize("hasAuthority('inventory:inbound:edit')")
    public Result<Void> updateInboundRecord(@Valid @RequestBody InventoryInboundDTO inboundDTO) {
        inventoryInboundService.updateInboundRecord(inboundDTO);
        return Result.success("入库记录修改成功");
    }

    @Operation(summary = "删除入库记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:inbound:remove')")
    public Result<Void> deleteInboundRecord(@Parameter(description = "入库记录ID") @PathVariable Long id) {
        inventoryInboundService.deleteInboundRecord(id);
        return Result.success("入库记录删除成功");
    }

    @Operation(summary = "批量删除入库记录")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('inventory:inbound:remove')")
    public Result<Void> batchDeleteInboundRecords(@Parameter(description = "入库记录ID列表") @RequestBody List<Long> ids) {
        inventoryInboundService.batchDeleteInboundRecords(ids);
        return Result.success("批量删除成功");
    }

    @Operation(summary = "审批入库单")
    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('inventory:inbound:edit')")
    public Result<Void> approve(@RequestParam Long id,
            @RequestParam(required = false) String remark) {
        inventoryInboundService.approve(id, remark);
        return Result.success("审批成功");
    }

    @Operation(summary = "确认入库单")
    @PostMapping("/confirm")
    @PreAuthorize("hasAuthority('inventory:inbound:edit')")
    public Result<Void> confirm(@RequestParam Long id,
            @RequestParam(required = false) String remark) {
        inventoryInboundService.confirm(id, remark);
        return Result.success("确认成功");
    }

    @Operation(summary = "取消入库单")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('inventory:inbound:edit')")
    public Result<Void> cancel(@RequestParam Long id,
            @RequestParam(required = false) String reason) {
        inventoryInboundService.cancel(id, reason);
        return Result.success("取消成功");
    }
}
