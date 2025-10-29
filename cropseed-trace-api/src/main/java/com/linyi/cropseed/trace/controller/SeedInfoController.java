package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.dto.SeedInfoAddDTO;
import com.linyi.cropseed.trace.dto.SeedInfoUpdateDTO;
import com.linyi.cropseed.trace.entity.SeedInfo;
import com.linyi.cropseed.trace.service.SeedInfoService;
import com.linyi.cropseed.trace.vo.SeedInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 种子信息Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "种子信息管理")
@RestController
@RequestMapping("/seed/info")
@RequiredArgsConstructor
public class SeedInfoController {

    private final SeedInfoService seedInfoService;

    @Operation(summary = "分页查询种子信息列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('seed:info:list')")
    public Result<PageResult<SeedInfoVO>> pageSeedInfos(PageQuery pageQuery,
                                                        @Parameter(description = "种子名称") @RequestParam(value = "seedName", required = false) String seedName,
                                                        @Parameter(description = "品种") @RequestParam(value = "variety", required = false) String variety,
                                                        @Parameter(description = "品类ID") @RequestParam(value = "categoryId", required = false) Long categoryId,
                                                        @Parameter(description = "状态") @RequestParam(value = "status", required = false) Integer status) {
        PageResult<SeedInfoVO> result = seedInfoService.pageSeedInfos(pageQuery, seedName, variety, categoryId, status);
        return Result.success(result);
    }

    @Operation(summary = "查询种子信息详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('seed:info:query')")
    public Result<SeedInfoVO> getSeedInfoById(@Parameter(description = "种子信息ID") @PathVariable Long id) {
        SeedInfoVO seedInfo = seedInfoService.getSeedInfoById(id);
        return Result.success(seedInfo);
    }

    @Operation(summary = "新增种子信息")
    @PostMapping
    @PreAuthorize("hasAuthority('seed:info:add')")
    public Result<Void> addSeedInfo(@Valid @RequestBody SeedInfoAddDTO seedInfoAddDTO) {
        SeedInfo seedInfo = new SeedInfo();
        seedInfo.setSeedName(seedInfoAddDTO.getSeedName());
        seedInfo.setSeedCode(seedInfoAddDTO.getSeedCode());
        seedInfo.setCategoryId(seedInfoAddDTO.getCategoryId());
        seedInfo.setVariety(seedInfoAddDTO.getVariety());
        seedInfo.setOriginPlace(seedInfoAddDTO.getOriginPlace());
        seedInfo.setCharacteristics(seedInfoAddDTO.getCharacteristics());
        seedInfo.setSpecifications(seedInfoAddDTO.getSpecifications());
        seedInfo.setImageUrl(seedInfoAddDTO.getImageUrl());
        seedInfo.setUnitPrice(seedInfoAddDTO.getUnitPrice());
        seedInfo.setStatus(seedInfoAddDTO.getStatus());

        seedInfoService.addSeedInfo(seedInfo);
        return Result.success("种子信息新增成功");
    }

    @Operation(summary = "修改种子信息")
    @PutMapping
    @PreAuthorize("hasAuthority('seed:info:edit')")
    public Result<Void> updateSeedInfo(@Valid @RequestBody SeedInfoUpdateDTO seedInfoUpdateDTO) {
        SeedInfo seedInfo = new SeedInfo();
        seedInfo.setId(seedInfoUpdateDTO.getId());
        seedInfo.setSeedName(seedInfoUpdateDTO.getSeedName());
        seedInfo.setSeedCode(seedInfoUpdateDTO.getSeedCode());
        seedInfo.setCategoryId(seedInfoUpdateDTO.getCategoryId());
        seedInfo.setVariety(seedInfoUpdateDTO.getVariety());
        seedInfo.setOriginPlace(seedInfoUpdateDTO.getOriginPlace());
        seedInfo.setCharacteristics(seedInfoUpdateDTO.getCharacteristics());
        seedInfo.setSpecifications(seedInfoUpdateDTO.getSpecifications());
        seedInfo.setImageUrl(seedInfoUpdateDTO.getImageUrl());
        seedInfo.setUnitPrice(seedInfoUpdateDTO.getUnitPrice());
        seedInfo.setStatus(seedInfoUpdateDTO.getStatus());

        seedInfoService.updateSeedInfo(seedInfo);
        return Result.success("种子信息修改成功");
    }

    @Operation(summary = "删除种子信息")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('seed:info:remove')")
    public Result<Void> deleteSeedInfo(@Parameter(description = "种子信息ID") @PathVariable Long id) {
        seedInfoService.deleteSeedInfo(id);
        return Result.success("种子信息删除成功");
    }

    @Operation(summary = "批量删除种子信息")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('seed:info:remove')")
    public Result<Void> batchDeleteSeedInfos(@Parameter(description = "种子信息ID列表") @RequestBody List<Long> ids) {
        seedInfoService.batchDeleteSeedInfos(ids);
        return Result.success("批量删除成功");
    }
}
