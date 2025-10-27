package com.linyi.cropseed.trace.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 入库记录VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class InventoryInboundVO {
    private Long id;
    private String batchNo;
    private Long warehouseId;
    private String warehouseName;
    private Long seedBatchId;
    private String seedBatchNo;
    private Integer quantity;
    private String supplier;
    private String qualityGrade;
    private String storageCondition;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
