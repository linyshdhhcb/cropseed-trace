package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.dto.SeedBatchAddDTO;
import com.linyi.cropseed.trace.dto.SeedBatchUpdateDTO;
import com.linyi.cropseed.trace.entity.SeedBatch;
import com.linyi.cropseed.trace.service.SeedBatchService;
import com.linyi.cropseed.trace.vo.SeedBatchVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 种子批次Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "种子批次管理")
@RestController
@RequestMapping("/seed/batch")
@RequiredArgsConstructor
public class SeedBatchController {

    private final SeedBatchService seedBatchService;

    @Operation(summary = "分页查询种子批次列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('seed:batch:list')")
    public Result<PageResult<SeedBatchVO>> pageSeedBatches(PageQuery pageQuery,
            @Parameter(description = "批次号") @RequestParam(required = false) String batchNo,
            @Parameter(description = "种子ID") @RequestParam(required = false) Long seedId) {
        PageResult<SeedBatchVO> result = seedBatchService.pageSeedBatches(pageQuery, batchNo, seedId);
        return Result.success(result);
    }

    @Operation(summary = "查询种子批次详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('seed:batch:query')")
    public Result<SeedBatchVO> getSeedBatchById(@Parameter(description = "种子批次ID") @PathVariable Long id) {
        SeedBatchVO seedBatch = seedBatchService.getSeedBatchById(id);
        return Result.success(seedBatch);
    }

    @Operation(summary = "新增种子批次")
    @PostMapping
    @PreAuthorize("hasAuthority('seed:batch:add')")
    public Result<Void> addSeedBatch(@Valid @RequestBody SeedBatchAddDTO seedBatchAddDTO) {
        SeedBatch seedBatch = new SeedBatch();
        seedBatch.setBatchNo(seedBatchAddDTO.getBatchNo());
        seedBatch.setSeedId(seedBatchAddDTO.getSeedId());
        seedBatch.setProductionDate(
                seedBatchAddDTO.getProductionDate() != null ? seedBatchAddDTO.getProductionDate().toLocalDate() : null);
        seedBatch.setExpiryDate(
                seedBatchAddDTO.getExpiryDate() != null ? seedBatchAddDTO.getExpiryDate().toLocalDate() : null);
        seedBatch.setQualityReport(seedBatchAddDTO.getQualityReport());
        seedBatch.setQualityStatus(seedBatchAddDTO.getQualityStatus());

        seedBatchService.addSeedBatch(seedBatch);
        return Result.success("种子批次新增成功");
    }

    @Operation(summary = "修改种子批次")
    @PutMapping
    @PreAuthorize("hasAuthority('seed:batch:edit')")
    public Result<Void> updateSeedBatch(@Valid @RequestBody SeedBatchUpdateDTO seedBatchUpdateDTO) {
        SeedBatch seedBatch = new SeedBatch();
        seedBatch.setId(seedBatchUpdateDTO.getId());
        seedBatch.setBatchNo(seedBatchUpdateDTO.getBatchNo());
        seedBatch.setSeedId(seedBatchUpdateDTO.getSeedId());
        seedBatch.setProductionDate(
                seedBatchUpdateDTO.getProductionDate() != null ? seedBatchUpdateDTO.getProductionDate().toLocalDate()
                        : null);
        seedBatch.setExpiryDate(
                seedBatchUpdateDTO.getExpiryDate() != null ? seedBatchUpdateDTO.getExpiryDate().toLocalDate() : null);
        seedBatch.setQualityReport(seedBatchUpdateDTO.getQualityReport());
        seedBatch.setQualityStatus(seedBatchUpdateDTO.getQualityStatus());

        seedBatchService.updateSeedBatch(seedBatch);
        return Result.success("种子批次修改成功");
    }

    @Operation(summary = "删除种子批次")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('seed:batch:remove')")
    public Result<Void> deleteSeedBatch(@Parameter(description = "种子批次ID") @PathVariable Long id) {
        seedBatchService.deleteSeedBatch(id);
        return Result.success("种子批次删除成功");
    }

    @Operation(summary = "批量删除种子批次")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('seed:batch:remove')")
    public Result<Void> batchDeleteSeedBatches(@Parameter(description = "种子批次ID列表") @RequestBody List<Long> ids) {
        seedBatchService.batchDeleteSeedBatches(ids);
        return Result.success("批量删除成功");
    }
}
