package com.linyi.cropseed.trace.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 出库记录VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class InventoryOutboundVO {
    private Long id;
    private String batchNo;
    private Long warehouseId;
    private String warehouseName;
    private Long seedBatchId;
    private String seedBatchNo;
    private Integer quantity;
    private String recipient;
    private String purpose;
    private Integer status;
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
