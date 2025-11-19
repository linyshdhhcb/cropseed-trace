package com.linyi.cropseed.trace.dto.blockchain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 链上溯源数据模型
 * 
 * @author linyi
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceChainData {
    
    /**
     * 溯源码
     */
    private String traceCode;
    
    /**
     * 批次ID
     */
    private Long batchId;
    
    /**
     * 记录类型
     */
    private Integer recordType;
    
    /**
     * 记录阶段
     */
    private String recordStage;
    
    /**
     * 操作人员
     */
    private String operatorName;
    
    /**
     * 记录时间
     */
    private LocalDateTime recordTime;
    
    /**
     * 位置信息
     */
    private String location;
    
    /**
     * 内容摘要
     */
    private String contentSummary;
    
    /**
     * 数据哈希
     */
    private String dataHash;
    
    /**
     * 上链时间戳
     */
    private Long timestamp;
    
    /**
     * 交易哈希
     */
    private String txId;
    
    /**
     * 实体名称
     */
    private String entityName;
    
    /**
     * 质量等级
     */
    private String qualityGrade;
    
    /**
     * 检测结果
     */
    private String testResult;
}
