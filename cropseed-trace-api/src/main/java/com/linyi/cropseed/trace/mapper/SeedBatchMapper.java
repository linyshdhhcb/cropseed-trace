package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.entity.SeedBatch;
import com.linyi.cropseed.trace.vo.SeedBatchVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 种子批次Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface SeedBatchMapper extends BaseMapper<SeedBatch> {

    /**
     * 根据种子ID查询批次列表
     */
    List<SeedBatch> selectBySeedId(Long seedId);

    /**
     * 根据批次号查询批次
     */
    SeedBatch selectByBatchNo(String batchNo);

    /**
     * 分页联表查询批次列表（带种子名称等信息）
     */
    Page<SeedBatchVO> selectPageVo(Page<SeedBatchVO> page,
            @Param("batchNo") String batchNo,
            @Param("seedId") Long seedId);

    /**
     * 联表查询批次详情
     */
    SeedBatchVO selectDetailById(@Param("id") Long id);
}
