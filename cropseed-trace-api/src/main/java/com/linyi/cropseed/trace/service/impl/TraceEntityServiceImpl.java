package com.linyi.cropseed.trace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.entity.TraceEntity;
import com.linyi.cropseed.trace.mapper.TraceEntityMapper;
import com.linyi.cropseed.trace.service.TraceEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 溯源实体管理服务实现
 *
 * @author linyi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TraceEntityServiceImpl implements TraceEntityService {

    private final TraceEntityMapper traceEntityMapper;

    @Override
    public PageResult<TraceEntity> getEntitiesPage(Integer pageNum, Integer pageSize,
                                                 Integer entityType, String entityName, Integer status) {

        // 构建查询条件
        LambdaQueryWrapper<TraceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(entityType != null, TraceEntity::getEntityType, entityType)
                   .like(StringUtils.hasText(entityName), TraceEntity::getEntityName, entityName)
                   .eq(status != null, TraceEntity::getStatus, status)
                   .orderByDesc(TraceEntity::getCreateTime);

        // 分页查询
        Page<TraceEntity> page = new Page<>(pageNum, pageSize);
        IPage<TraceEntity> result = traceEntityMapper.selectPage(page, queryWrapper);

        // 构建分页结果
        PageResult<TraceEntity> pageResult = new PageResult<>();
        pageResult.setList(result.getRecords());
        pageResult.setTotal(result.getTotal());
        pageResult.setPageNum(result.getCurrent());
        pageResult.setPageSize(result.getSize());

        log.info("分页查询溯源实体完成: 共{}条记录", result.getTotal());
        return pageResult;
    }

    @Override
    public TraceEntity getEntityById(Long id) {
        TraceEntity entity = traceEntityMapper.selectById(id);
        log.info("根据ID查询溯源实体: id={}, entity={}", id, entity != null ? "找到" : "未找到");
        return entity;
    }

    @Override
    public void createEntity(TraceEntity entity) {
        // 设置默认状态为启用
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }

        int result = traceEntityMapper.insert(entity);
        if (result > 0) {
            log.info("创建溯源实体成功: {}", entity);
        } else {
            throw new RuntimeException("创建溯源实体失败");
        }
    }

    @Override
    public void updateEntity(TraceEntity entity) {
        TraceEntity existingEntity = traceEntityMapper.selectById(entity.getId());
        if (existingEntity == null) {
            throw new RuntimeException("溯源实体不存在");
        }

        int result = traceEntityMapper.updateById(entity);
        if (result > 0) {
            log.info("更新溯源实体成功: {}", entity);
        } else {
            throw new RuntimeException("更新溯源实体失败");
        }
    }

    @Override
    public void deleteEntity(Long id) {
        TraceEntity existingEntity = traceEntityMapper.selectById(id);
        if (existingEntity == null) {
            throw new RuntimeException("溯源实体不存在");
        }

        int result = traceEntityMapper.deleteById(id);
        if (result > 0) {
            log.info("删除溯源实体成功: id={}", id);
        } else {
            throw new RuntimeException("删除溯源实体失败");
        }
    }

    @Override
    public TraceEntity getEntityByTypeAndCode(Integer entityType, String entityCode) {
        LambdaQueryWrapper<TraceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TraceEntity::getEntityType, entityType)
                   .eq(TraceEntity::getEntityCode, entityCode)
                   .last("LIMIT 1");

        TraceEntity entity = traceEntityMapper.selectOne(queryWrapper);
        log.info("根据类型和编码查询实体: entityType={}, entityCode={}, result={}",
               entityType, entityCode, entity != null ? "找到" : "未找到");
        return entity;
    }

    @Override
    public void toggleEntityStatus(Long id) {
        TraceEntity entity = traceEntityMapper.selectById(id);
        if (entity == null) {
            throw new RuntimeException("溯源实体不存在");
        }

        // 切换状态：1启用 <-> 0禁用
        Integer newStatus = entity.getStatus() == 1 ? 0 : 1;
        entity.setStatus(newStatus);

        int result = traceEntityMapper.updateById(entity);
        if (result > 0) {
            log.info("切换实体状态成功: id={}, 新状态={}", id, newStatus == 1 ? "启用" : "禁用");
        } else {
            throw new RuntimeException("切换实体状态失败");
        }
    }
}
