package com.linyi.cropseed.trace.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 种子批次VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SeedBatchVO {
    private Long id;
    private String batchNo;
    private Long seedInfoId;
    private String seedName;
    private Integer quantity;
    private LocalDateTime productionDate;
    private LocalDateTime expiryDate;
    private String supplier;
    private String qualityGrade;
    private String storageCondition;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
