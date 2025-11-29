package com.linyi.cropseed.trace.module.system.service;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.module.system.model.entity.SysConfig;
import com.linyi.cropseed.trace.module.system.model.vo.SysConfigVO;

import java.util.List;

/**
 * 系统配置Service接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface SysConfigService {

    /**
     * 分页查询配置列表
     */
    PageResult<SysConfigVO> pageConfigs(PageQuery pageQuery, String configKey, String configName, Integer configType);

    /**
     * 根据ID查询配置详情
     */
    SysConfigVO getConfigById(Long id);

    /**
     * 根据配置键查询配置值
     */
    String getConfigValueByKey(String configKey);

    /**
     * 根据配置键查询配置
     */
    SysConfigVO getConfigByKey(String configKey);

    /**
     * 新增配置
     */
    void addConfig(SysConfig config);

    /**
     * 修改配置
     */
    void updateConfig(SysConfig config);

    /**
     * 删除配置
     */
    void deleteConfig(Long id);

    /**
     * 批量删除配置
     */
    void batchDeleteConfigs(List<Long> ids);

    /**
     * 刷新配置缓存
     */
    void refreshCache();

    /**
     * 获取所有配置列表
     */
    List<SysConfigVO> getAllConfigs();

    /**
     * 根据配置类型获取配置列表
     */
    List<SysConfigVO> getConfigsByType(Integer configType);
}
