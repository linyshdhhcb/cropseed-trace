package com.linyi.cropseed.trace.module.trace.controller;

import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.trace.model.entity.TraceEntity;
import com.linyi.cropseed.trace.module.trace.service.TraceEntityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 溯源实体管理Controller
 *
 * @author linyi
 */
@Slf4j
@RestController
@RequestMapping("/trace/entities")
@RequiredArgsConstructor
@Tag(name = "溯源实体管理", description = "溯源实体的增删改查操作")
public class TraceEntityController {

    private final TraceEntityService traceEntityService;

    @Operation(summary = "分页查询溯源实体")
    @GetMapping("/page")
    public Result<PageResult<TraceEntity>> getEntitiesPage(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "实体类型") @RequestParam(required = false) Integer entityType,
            @Parameter(description = "实体名称") @RequestParam(required = false) String entityName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        log.info("分页查询溯源实体: pageNum={}, pageSize={}, entityType={}, entityName={}, status={}",
                pageNum, pageSize, entityType, entityName, status);

        try {
            PageResult<TraceEntity> pageResult = traceEntityService.getEntitiesPage(
                pageNum, pageSize, entityType, entityName, status);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("分页查询溯源实体失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据ID查询溯源实体")
    @GetMapping("/{id}")
    public Result<TraceEntity> getEntityById(@PathVariable Long id) {
        log.info("根据ID查询溯源实体: {}", id);

        try {
            TraceEntity entity = traceEntityService.getEntityById(id);
            if (entity == null) {
                return Result.fail("溯源实体不存在");
            }
            return Result.success(entity);
        } catch (Exception e) {
            log.error("查询溯源实体失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "创建溯源实体")
    @PostMapping
    public Result<String> createEntity(@RequestBody TraceEntity entity) {
        log.info("创建溯源实体: {}", entity);

        try {
            traceEntityService.createEntity(entity);
            return Result.success("创建成功");
        } catch (Exception e) {
            log.error("创建溯源实体失败", e);
            return Result.fail("创建失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新溯源实体")
    @PutMapping("/{id}")
    public Result<String> updateEntity(@PathVariable Long id, @RequestBody TraceEntity entity) {
        log.info("更新溯源实体: id={}, entity={}", id, entity);

        try {
            entity.setId(id);
            traceEntityService.updateEntity(entity);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新溯源实体失败", e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除溯源实体")
    @DeleteMapping("/{id}")
    public Result<String> deleteEntity(@PathVariable Long id) {
        log.info("删除溯源实体: {}", id);

        try {
            traceEntityService.deleteEntity(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除溯源实体失败", e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据类型和编码查询实体")
    @GetMapping("/search")
    public Result<TraceEntity> getEntityByTypeAndCode(
            @Parameter(description = "实体类型") @RequestParam Integer entityType,
            @Parameter(description = "实体编码") @RequestParam String entityCode) {

        log.info("根据类型和编码查询实体: entityType={}, entityCode={}", entityType, entityCode);

        try {
            TraceEntity entity = traceEntityService.getEntityByTypeAndCode(entityType, entityCode);
            return Result.success(entity);
        } catch (Exception e) {
            log.error("查询溯源实体失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "切换实体状态")
    @PutMapping("/{id}/status")
    public Result<String> toggleEntityStatus(@PathVariable Long id) {
        log.info("切换实体状态: {}", id);

        try {
            traceEntityService.toggleEntityStatus(id);
            return Result.success("状态切换成功");
        } catch (Exception e) {
            log.error("切换实体状态失败", e);
            return Result.fail("操作失败: " + e.getMessage());
        }
    }
}
