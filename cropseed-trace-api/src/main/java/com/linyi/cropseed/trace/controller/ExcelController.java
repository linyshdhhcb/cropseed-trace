package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.service.ExcelService;
import com.linyi.cropseed.trace.vo.ExcelImportResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Excel管理Controller
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "Excel管理")
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    @Operation(summary = "导入种子信息")
    @PostMapping("/import/seeds")
    @PreAuthorize("hasAuthority('excel:import')")
    public Result<ExcelImportResultVO> importSeeds(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) {
        ExcelImportResultVO result = excelService.importSeeds(file);
        return Result.success("种子信息导入完成", result);
    }

    @Operation(summary = "导入库存信息")
    @PostMapping("/import/inventory")
    @PreAuthorize("hasAuthority('excel:import')")
    public Result<ExcelImportResultVO> importInventory(
            @Parameter(description = "Excel文件") @RequestParam("file") MultipartFile file) {
        ExcelImportResultVO result = excelService.importInventory(file);
        return Result.success("库存信息导入完成", result);
    }

    @Operation(summary = "下载种子信息模板")
    @GetMapping("/template/seeds")
    @PreAuthorize("hasAuthority('excel:export')")
    public void exportSeedTemplate(HttpServletResponse response) {
        excelService.exportSeedTemplate(response);
    }

    @Operation(summary = "下载库存信息模板")
    @GetMapping("/template/inventory")
    @PreAuthorize("hasAuthority('excel:export')")
    public void exportInventoryTemplate(HttpServletResponse response) {
        excelService.exportInventoryTemplate(response);
    }

    @Operation(summary = "导出种子信息")
    @GetMapping("/export/seeds")
    @PreAuthorize("hasAuthority('excel:export')")
    public void exportSeeds(HttpServletResponse response,
            @Parameter(description = "种子ID列表") @RequestParam(required = false) List<Long> seedIds) {
        excelService.exportSeeds(response, seedIds);
    }

    @Operation(summary = "导出库存信息")
    @GetMapping("/export/inventory")
    @PreAuthorize("hasAuthority('excel:export')")
    public void exportInventory(HttpServletResponse response,
            @Parameter(description = "库存ID列表") @RequestParam(required = false) List<Long> inventoryIds) {
        excelService.exportInventory(response, inventoryIds);
    }

    @Operation(summary = "导出订单信息")
    @GetMapping("/export/orders")
    @PreAuthorize("hasAuthority('excel:export')")
    public void exportOrders(HttpServletResponse response,
            @Parameter(description = "订单ID列表") @RequestParam(required = false) List<Long> orderIds) {
        excelService.exportOrders(response, orderIds);
    }
}
