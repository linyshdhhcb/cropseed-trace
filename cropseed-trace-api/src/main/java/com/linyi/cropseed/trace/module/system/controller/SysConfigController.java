package com.linyi.cropseed.trace.module.system.controller;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.system.model.dto.SysConfigAddDTO;
import com.linyi.cropseed.trace.module.system.model.dto.SysConfigUpdateDTO;
import com.linyi.cropseed.trace.module.system.model.entity.SysConfig;
import com.linyi.cropseed.trace.module.system.service.SysConfigService;
import com.linyi.cropseed.trace.module.system.model.vo.SysConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置Controller
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "系统配置管理")
@RestController
@RequestMapping("/sys/config")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    @Operation(summary = "分页查询配置列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:config:list')")
    public Result<PageResult<SysConfigVO>> pageConfigs(PageQuery pageQuery,
            @Parameter(description = "配置键") @RequestParam(required = false) String configKey,
            @Parameter(description = "配置名称") @RequestParam(required = false) String configName,
            @Parameter(description = "配置类型") @RequestParam(required = false) Integer configType) {
        PageResult<SysConfigVO> result = sysConfigService.pageConfigs(pageQuery, configKey, configName, configType);
        return Result.success(result);
    }

    @Operation(summary = "查询配置详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<SysConfigVO> getConfigById(@Parameter(description = "配置ID") @PathVariable Long id) {
        SysConfigVO config = sysConfigService.getConfigById(id);
        return Result.success(config);
    }

    @Operation(summary = "根据配置键查询配置值")
    @GetMapping("/key/{configKey}")
    public Result<String> getConfigValueByKey(@Parameter(description = "配置键") @PathVariable String configKey) {
        String value = sysConfigService.getConfigValueByKey(configKey);
        return Result.success(value);
    }

    @Operation(summary = "根据配置键查询配置")
    @GetMapping("/info/{configKey}")
    @PreAuthorize("hasAuthority('system:config:query')")
    public Result<SysConfigVO> getConfigByKey(@Parameter(description = "配置键") @PathVariable String configKey) {
        SysConfigVO config = sysConfigService.getConfigByKey(configKey);
        return Result.success(config);
    }

    @Operation(summary = "新增配置")
    @PostMapping
    @PreAuthorize("hasAuthority('system:config:add')")
    public Result<Void> addConfig(@Valid @RequestBody SysConfigAddDTO configAddDTO) {
        SysConfig config = new SysConfig();
        config.setConfigKey(configAddDTO.getConfigKey());
        config.setConfigValue(configAddDTO.getConfigValue());
        config.setConfigName(configAddDTO.getConfigName());
        config.setConfigType(configAddDTO.getConfigType());
        config.setDescription(configAddDTO.getDescription());

        sysConfigService.addConfig(config);
        return Result.success("配置新增成功");
    }

    @Operation(summary = "修改配置")
    @PutMapping
    @PreAuthorize("hasAuthority('system:config:edit')")
    public Result<Void> updateConfig(@Valid @RequestBody SysConfigUpdateDTO configUpdateDTO) {
        SysConfig config = new SysConfig();
        config.setId(configUpdateDTO.getId());
        config.setConfigKey(configUpdateDTO.getConfigKey());
        config.setConfigValue(configUpdateDTO.getConfigValue());
        config.setConfigName(configUpdateDTO.getConfigName());
        config.setConfigType(configUpdateDTO.getConfigType());
        config.setDescription(configUpdateDTO.getDescription());

        sysConfigService.updateConfig(config);
        return Result.success("配置修改成功");
    }

    @Operation(summary = "删除配置")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:config:remove')")
    public Result<Void> deleteConfig(@Parameter(description = "配置ID") @PathVariable Long id) {
        sysConfigService.deleteConfig(id);
        return Result.success("配置删除成功");
    }

    @Operation(summary = "批量删除配置")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('system:config:remove')")
    public Result<Void> batchDeleteConfigs(@Parameter(description = "配置ID列表") @RequestBody List<Long> ids) {
        sysConfigService.batchDeleteConfigs(ids);
        return Result.success("批量删除成功");
    }

    @Operation(summary = "刷新配置缓存")
    @PostMapping("/refresh-cache")
    @PreAuthorize("hasAuthority('system:config:edit')")
    public Result<Void> refreshCache() {
        sysConfigService.refreshCache();
        return Result.success("缓存刷新成功");
    }

    @Operation(summary = "获取所有配置列表")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:config:list')")
    public Result<List<SysConfigVO>> getAllConfigs() {
        List<SysConfigVO> configs = sysConfigService.getAllConfigs();
        return Result.success(configs);
    }

    @Operation(summary = "根据配置类型获取配置列表")
    @GetMapping("/type/{configType}")
    @PreAuthorize("hasAuthority('system:config:list')")
    public Result<List<SysConfigVO>> getConfigsByType(@Parameter(description = "配置类型") @PathVariable Integer configType) {
        List<SysConfigVO> configs = sysConfigService.getConfigsByType(configType);
        return Result.success(configs);
    }
}
