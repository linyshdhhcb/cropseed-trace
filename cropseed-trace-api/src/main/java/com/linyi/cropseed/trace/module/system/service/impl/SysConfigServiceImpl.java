package com.linyi.cropseed.trace.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.module.system.mapper.SysConfigMapper;
import com.linyi.cropseed.trace.module.system.model.entity.SysConfig;
import com.linyi.cropseed.trace.module.system.service.SysConfigService;
import com.linyi.cropseed.trace.module.system.model.vo.SysConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统配置Service实现类
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements SysConfigService {

    private static final String CACHE_CONFIG = "sys:config";
    private static final String CACHE_CONFIG_ALL = "sys:config:all";
    private static final String CACHE_CONFIG_KEY = "sys:config:key";

    private final SysConfigMapper sysConfigMapper;

    @Override
    public PageResult<SysConfigVO> pageConfigs(PageQuery pageQuery, String configKey, String configName, Integer configType) {
        Page<SysConfig> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(configKey), SysConfig::getConfigKey, configKey)
                .like(StringUtils.hasText(configName), SysConfig::getConfigName, configName)
                .eq(configType != null, SysConfig::getConfigType, configType)
                .orderByDesc(SysConfig::getId);

        Page<SysConfig> result = sysConfigMapper.selectPage(page, queryWrapper);

        List<SysConfigVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result, voList);
    }

    @Override
    @Cacheable(value = CACHE_CONFIG, key = "#id", unless = "#result == null")
    public SysConfigVO getConfigById(Long id) {
        SysConfig config = sysConfigMapper.selectById(id);
        return config != null ? convertToVO(config) : null;
    }

    @Override
    @Cacheable(value = CACHE_CONFIG_KEY, key = "#configKey", unless = "#result == null")
    public String getConfigValueByKey(String configKey) {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig config = sysConfigMapper.selectOne(queryWrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    @Cacheable(value = CACHE_CONFIG_KEY, key = "'obj:' + #configKey", unless = "#result == null")
    public SysConfigVO getConfigByKey(String configKey) {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig config = sysConfigMapper.selectOne(queryWrapper);
        return config != null ? convertToVO(config) : null;
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG, CACHE_CONFIG_ALL, CACHE_CONFIG_KEY}, allEntries = true)
    public void addConfig(SysConfig config) {
        // 检查配置键是否已存在
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getConfigKey, config.getConfigKey());
        if (sysConfigMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "配置键已存在");
        }
        sysConfigMapper.insert(config);
        log.info("新增配置成功: {}", config.getConfigKey());
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG, CACHE_CONFIG_ALL, CACHE_CONFIG_KEY}, allEntries = true)
    public void updateConfig(SysConfig config) {
        // 检查配置键是否被其他配置使用
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getConfigKey, config.getConfigKey())
                .ne(SysConfig::getId, config.getId());
        if (sysConfigMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "配置键已被其他配置使用");
        }
        sysConfigMapper.updateById(config);
        log.info("修改配置成功: {}", config.getConfigKey());
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG, CACHE_CONFIG_ALL, CACHE_CONFIG_KEY}, allEntries = true)
    public void deleteConfig(Long id) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "配置不存在");
        }
        sysConfigMapper.deleteById(id);
        log.info("删除配置成功: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CACHE_CONFIG, CACHE_CONFIG_ALL, CACHE_CONFIG_KEY}, allEntries = true)
    public void batchDeleteConfigs(List<Long> ids) {
        sysConfigMapper.deleteBatchIds(ids);
        log.info("批量删除配置成功: {}", ids);
    }

    @Override
    @CacheEvict(value = {CACHE_CONFIG, CACHE_CONFIG_ALL, CACHE_CONFIG_KEY}, allEntries = true)
    public void refreshCache() {
        log.info("刷新配置缓存成功");
    }

    @Override
    @Cacheable(value = CACHE_CONFIG_ALL, unless = "#result == null || #result.isEmpty()")
    public List<SysConfigVO> getAllConfigs() {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SysConfig::getId);
        List<SysConfig> configs = sysConfigMapper.selectList(queryWrapper);
        return configs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SysConfigVO> getConfigsByType(Integer configType) {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getConfigType, configType)
                .orderByDesc(SysConfig::getId);
        List<SysConfig> configs = sysConfigMapper.selectList(queryWrapper);
        return configs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private SysConfigVO convertToVO(SysConfig config) {
        SysConfigVO vo = new SysConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }
}
