package com.linyi.cropseed.trace.module.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.module.trace.model.entity.TraceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 溯源实体表 Mapper 接口
 *
 * @author linyi
 * @since 2024-01-01
 */
@Mapper
public interface TraceEntityMapper extends BaseMapper<TraceEntity> {

    /**
     * 根据实体类型查询
     * @param entityType 实体类型
     * @return 实体列表
     */
    List<TraceEntity> selectByEntityType(@Param("entityType") Integer entityType);

    /**
     * 根据实体编码查询
     * @param entityCode 实体编码
     * @return 实体信息
     */
    TraceEntity selectByEntityCode(@Param("entityCode") String entityCode);
}
