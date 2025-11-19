package com.linyi.cropseed.trace.service;

import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.entity.TraceEntity;

/**
 * 溯源实体管理服务接口
 * 
 * @author linyi
 */
public interface TraceEntityService {

    /**
     * 分页查询溯源实体
     */
    PageResult<TraceEntity> getEntitiesPage(Integer pageNum, Integer pageSize, 
                                          Integer entityType, String entityName, Integer status);

    /**
     * 根据ID查询溯源实体
     */
    TraceEntity getEntityById(Long id);

    /**
     * 创建溯源实体
     */
    void createEntity(TraceEntity entity);

    /**
     * 更新溯源实体
     */
    void updateEntity(TraceEntity entity);

    /**
     * 删除溯源实体
     */
    void deleteEntity(Long id);

    /**
     * 根据类型和编码查询实体
     */
    TraceEntity getEntityByTypeAndCode(Integer entityType, String entityCode);

    /**
     * 切换实体状态
     */
    void toggleEntityStatus(Long id);
}
