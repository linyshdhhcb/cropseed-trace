package com.linyi.cropseed.trace.module.system.controller;

import com.linyi.cropseed.trace.common.annotation.OperationLog;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.system.model.dto.SysOperationLogBatchDeleteDTO;
import com.linyi.cropseed.trace.module.system.model.dto.SysOperationLogQueryDTO;
import com.linyi.cropseed.trace.module.system.model.vo.SysOperationLogVO;
import com.linyi.cropseed.trace.module.system.service.SysOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志Controller
 *
 * @author LinYi
 * @since 2025-11-28
 */
@Tag(name = "操作日志管理")
@RestController
@RequestMapping("/sys/operation-log")
@RequiredArgsConstructor
public class SysOperationLogController {

    private final SysOperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:log:list')")
    public Result<PageResult<SysOperationLogVO>> pageLog(PageQuery pageQuery, SysOperationLogQueryDTO queryDTO) {
        PageResult<SysOperationLogVO> result = operationLogService.pageLogs(pageQuery, queryDTO);
        return Result.success(result);
    }

    @Operation(summary = "查询操作日志详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:log:query')")
    public Result<SysOperationLogVO> getLogById(@Parameter(description = "日志ID") @PathVariable Long id) {
        SysOperationLogVO log = operationLogService.getLogById(id);
        return Result.success(log);
    }

    @OperationLog(module = "操作日志管理", content = "删除操作日志")
    @Operation(summary = "删除操作日志")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:log:delete')")
    public Result<Void> deleteLog(@Parameter(description = "日志ID") @PathVariable Long id) {
        operationLogService.deleteLog(id);
        return Result.success();
    }

    @OperationLog(module = "操作日志管理", content = "批量删除操作日志")
    @Operation(summary = "批量删除操作日志")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:log:delete')")
    public Result<Void> batchDeleteLogs(@Valid @RequestBody SysOperationLogBatchDeleteDTO dto) {
        operationLogService.batchDeleteLogs(dto.getIds());
        return Result.success();
    }

    @OperationLog(module = "操作日志管理", content = "清空操作日志")
    @Operation(summary = "清空操作日志")
    @DeleteMapping("/clear")
    @PreAuthorize("hasAuthority('system:log:delete')")
    public Result<Void> clearLogs() {
        operationLogService.clearLogs();
        return Result.success();
    }
}
