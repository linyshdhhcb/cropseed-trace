package com.linyi.cropseed.trace.module.trace.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.cropseed.trace.module.trace.model.vo.BlockchainProof;
import com.linyi.cropseed.trace.module.trace.model.entity.TraceRecord;

import java.util.List;

/**
 * 溯源记录服务接口
 *
 * @author linyi
 * @since 2024-01-01
 */
public interface TraceRecordService extends IService<TraceRecord> {

    /**
     * 创建溯源记录（包含区块链上链）
     * @param traceRecord 溯源记录
     * @return 记录ID
     */
    Long createTraceRecord(TraceRecord traceRecord);

    /**
     * 根据溯源码查询完整溯源链
     * @param traceCode 溯源码
     * @return 溯源记录列表
     */
    List<TraceRecord> getTraceChain(String traceCode);

    /**
     * 分页查询溯源记录
     * @param page 分页信息
     * @param traceCode 溯源码（可选）
     * @param batchId 批次ID（可选）
     * @param recordType 记录类型（可选）
     * @return 分页结果
     */
    Page<TraceRecord> getTraceRecordsPage(Page<TraceRecord> page, String traceCode, Long batchId, Integer recordType);

    /**
     * 验证溯源数据完整性
     * @param traceCode 溯源码
     * @return 验证结果
     */
    boolean verifyTraceIntegrity(String traceCode);

    /**
     * 获取溯源证明
     * @param traceCode 溯源码
     * @return 区块链证明
     */
    BlockchainProof getTraceProof(String traceCode);

    /**
     * 更新区块链状态
     * @param recordId 记录ID
     * @param txHash 交易哈希
     * @param status 状态
     */
    void updateBlockchainStatus(Long recordId, String txHash, Integer status);

    /**
     * 单个记录上链
     * @param recordId 记录ID
     */
    void uploadSingleRecordToBlockchain(Long recordId);

    /**
     * 批量上链未上链的记录
     * @param limit 处理数量限制
     * @return 处理数量
     */
    int batchUploadToBlockchain(Integer limit);
}
