package com.linyi.cropseed.trace.module.trace.service;

import com.linyi.cropseed.trace.module.trace.model.vo.BlockchainProof;
import com.linyi.cropseed.trace.module.trace.model.vo.TraceChainData;
import com.linyi.cropseed.trace.module.trace.model.entity.TraceRecord;

/**
 * 区块链交互服务接口
 *
 * @author linyi
 * @since 2024-01-01
 */
public interface BlockchainService {

    /**
     * 上链存储溯源记录
     * @param traceRecord 溯源记录
     * @return 区块链交易哈希
     */
    String uploadTraceRecord(TraceRecord traceRecord);

    /**
     * 查询链上溯源记录
     * @param traceCode 溯源码
     * @return 溯源记录
     */
    TraceChainData queryChainTraceRecord(String traceCode);

    /**
     * 验证溯源数据完整性
     * @param traceCode 溯源码
     * @param localRecord 本地记录
     * @return 验证结果
     */
    boolean verifyTraceIntegrity(String traceCode, TraceRecord localRecord);

    /**
     * 获取溯源证明
     * @param traceCode 溯源码
     * @return 区块链证明
     */
    BlockchainProof generateTraceProof(String traceCode);

    /**
     * 查询交易状态
     * @param txHash 交易哈希
     * @return 交易状态
     */
    boolean isTransactionConfirmed(String txHash);
}
