package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.dto.InventoryInboundDTO;
import com.linyi.cropseed.trace.dto.InventoryOutboundDTO;
import com.linyi.cropseed.trace.entity.SeedBatch;
import com.linyi.cropseed.trace.mapper.SeedBatchMapper;
import com.linyi.cropseed.trace.service.InventoryService;
import com.linyi.cropseed.trace.vo.InventoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存管理Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "库存管理")
@RestController
@RequestMapping("/inventory/stock")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final SeedBatchMapper seedBatchMapper;

    @Operation(summary = "分页查询库存列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('inventory:list')")
    public Result<PageResult<InventoryVO>> pageInventories(PageQuery pageQuery,
            @Parameter(description = "种子ID") @RequestParam(required = false) Long seedId,
            @Parameter(description = "仓库ID") @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "种子名称") @RequestParam(required = false) String seedName) {
        PageResult<InventoryVO> result = inventoryService.pageInventories(pageQuery, seedId, warehouseId, seedName);
        return Result.success(result);
    }

    @Operation(summary = "根据种子ID查询库存")
    @GetMapping("/seed/{seedId}")
    @PreAuthorize("hasAuthority('inventory:list')")
    public Result<List<InventoryVO>> getInventoriesBySeedId(
            @Parameter(description = "种子ID") @PathVariable Long seedId) {
        List<InventoryVO> inventories = inventoryService.getInventoriesBySeedId(seedId);
        return Result.success(inventories);
    }

    @Operation(summary = "根据仓库ID查询库存")
    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAuthority('inventory:list')")
    public Result<List<InventoryVO>> getInventoriesByWarehouseId(
            @Parameter(description = "仓库ID") @PathVariable Long warehouseId) {
        List<InventoryVO> inventories = inventoryService.getInventoriesByWarehouseId(warehouseId);
        return Result.success(inventories);
    }

    @Operation(summary = "入库操作")
    @PostMapping("/inbound")
    @PreAuthorize("hasAuthority('inventory:inbound')")
    public Result<Void> inbound(@Valid @RequestBody InventoryInboundDTO inboundDTO) {
        // 根据种子批次ID获取种子ID
        SeedBatch seedBatch = seedBatchMapper.selectById(inboundDTO.getSeedBatchId());
        if (seedBatch == null || seedBatch.getSeedId() == null) {
            return Result.fail("种子批次不存在或缺少种子信息");
        }

        inventoryService.inbound(
                seedBatch.getSeedId(),
                inboundDTO.getSeedBatchId(),
                inboundDTO.getWarehouseId(),
                inboundDTO.getQuantity(),
                inboundDTO.getRemark());
        return Result.success("入库成功");
    }

    @Operation(summary = "出库操作")
    @PostMapping("/outbound")
    @PreAuthorize("hasAuthority('inventory:outbound')")
    public Result<Void> outbound(@Valid @RequestBody InventoryOutboundDTO outboundDTO) {
        inventoryService.outbound(
                outboundDTO.getSeedId(),
                outboundDTO.getBatchId(),
                outboundDTO.getWarehouseId(),
                outboundDTO.getQuantity(),
                outboundDTO.getPurpose(),
                outboundDTO.getOrderId(),
                outboundDTO.getRemarks());
        return Result.success("出库成功");
    }

    @Operation(summary = "锁定库存")
    @PostMapping("/lock")
    @PreAuthorize("hasAuthority('inventory:lock')")
    public Result<Void> lockInventory(@Parameter(description = "种子ID") @RequestParam Long seedId,
            @Parameter(description = "批次ID") @RequestParam Long batchId,
            @Parameter(description = "仓库ID") @RequestParam Long warehouseId,
            @Parameter(description = "锁定数量") @RequestParam Integer quantity) {
        inventoryService.lockInventory(seedId, batchId, warehouseId, quantity);
        return Result.success("库存锁定成功");
    }

    @Operation(summary = "释放锁定库存")
    @PostMapping("/unlock")
    @PreAuthorize("hasAuthority('inventory:unlock')")
    public Result<Void> unlockInventory(@Parameter(description = "种子ID") @RequestParam Long seedId,
            @Parameter(description = "批次ID") @RequestParam Long batchId,
            @Parameter(description = "仓库ID") @RequestParam Long warehouseId,
            @Parameter(description = "释放数量") @RequestParam Integer quantity) {
        inventoryService.unlockInventory(seedId, batchId, warehouseId, quantity);
        return Result.success("库存解锁成功");
    }

    @Operation(summary = "扣减库存")
    @PostMapping("/deduct")
    @PreAuthorize("hasAuthority('inventory:deduct')")
    public Result<Void> deductInventory(@Parameter(description = "种子ID") @RequestParam Long seedId,
            @Parameter(description = "批次ID") @RequestParam Long batchId,
            @Parameter(description = "仓库ID") @RequestParam Long warehouseId,
            @Parameter(description = "扣减数量") @RequestParam Integer quantity) {
        inventoryService.deductInventory(seedId, batchId, warehouseId, quantity);
        return Result.success("库存扣减成功");
    }

    @Operation(summary = "获取库存详情")
    @GetMapping("/detail")
    @PreAuthorize("hasAuthority('inventory:query')")
    public Result<InventoryVO> getInventoryDetail(@Parameter(description = "种子ID") @RequestParam Long seedId,
            @Parameter(description = "批次ID") @RequestParam Long batchId,
            @Parameter(description = "仓库ID") @RequestParam Long warehouseId) {
        InventoryVO inventory = inventoryService.getInventoryDetail(seedId, batchId, warehouseId);
        return Result.success(inventory);
    }

    @Operation(summary = "获取库存预警列表")
    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('inventory:list')")
    public Result<List<InventoryVO>> getLowStockInventories() {
        List<InventoryVO> inventories = inventoryService.getLowStockInventories();
        return Result.success(inventories);
    }

    @Operation(summary = "更新库存预警阈值")
    @PutMapping("/threshold")
    @PreAuthorize("hasAuthority('inventory:edit')")
    public Result<Void> updateStockThreshold(@Parameter(description = "库存ID") @RequestParam Long id,
            @Parameter(description = "最低库存") @RequestParam Integer minStock,
            @Parameter(description = "最高库存") @RequestParam Integer maxStock) {
        inventoryService.updateStockThreshold(id, minStock, maxStock);
        return Result.success("库存预警阈值更新成功");
    }

    @Operation(summary = "调整库存")
    @PostMapping("/adjust")
    @PreAuthorize("hasAuthority('inventory:edit')")
    public Result<Void> adjust(@Parameter(description = "库存ID") @RequestParam Long id,
            @Parameter(description = "调整类型 increase/decrease") @RequestParam String adjustType,
            @Parameter(description = "调整数量") @RequestParam Integer adjustQuantity,
            @Parameter(description = "原因") @RequestParam(required = false) String reason) {
        inventoryService.adjustInventory(id, adjustType, adjustQuantity, reason);
        return Result.success("库存调整成功");
    }
}
