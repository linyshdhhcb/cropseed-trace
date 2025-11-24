package com.linyi.cropseed.trace.module.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.module.trace.model.entity.TraceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 溯源记录表 Mapper 接口
 *
 * @author linyi
 * @since 2024-01-01
 */
@Mapper
public interface TraceRecordMapper extends BaseMapper<TraceRecord> {

    /**
     * 根据溯源码查询所有记录
     * @param traceCode 溯源码
     * @return 溯源记录列表
     */
    List<TraceRecord> selectByTraceCode(@Param("traceCode") String traceCode);

    /**
     * 根据批次ID查询记录
     * @param batchId 批次ID
     * @return 溯源记录列表
     */
    List<TraceRecord> selectByBatchId(@Param("batchId") Long batchId);

    /**
     * 根据记录类型统计数量
     * @param recordType 记录类型
     * @return 记录数量
     */
    Long countByRecordType(@Param("recordType") Integer recordType);

    /**
     * 更新区块链状态
     * @param id 记录ID
     * @param txHash 交易哈希
     * @param status 区块链状态
     */
    void updateBlockchainStatus(@Param("id") Long id, @Param("txHash") String txHash, @Param("status") Integer status);
}
