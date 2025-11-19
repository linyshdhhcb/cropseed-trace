package com.linyi.cropseed.trace.dto.blockchain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 区块链证明
 * 
 * @author linyi
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainProof {
    
    /**
     * 溯源码
     */
    private String traceCode;
    
    /**
     * 交易哈希
     */
    private String txHash;
    
    /**
     * 区块高度
     */
    private Long blockHeight;
    
    /**
     * 交易时间戳
     */
    private Long timestamp;
    
    /**
     * 数据哈希
     */
    private String dataHash;
    
    /**
     * 消耗Gas
     */
    private Long gasUsed;
    
    /**
     * 交易状态
     */
    private Integer txStatus;
    
    /**
     * 验证状态
     */
    private Boolean verified;
}
