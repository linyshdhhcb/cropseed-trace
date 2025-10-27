package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.linyi.cropseed.trace.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 种子批次实体
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seed_batch")
@Schema(description = "种子批次")
public class SeedBatch extends BaseEntity {

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "种子ID")
    private Long seedId;

    @Schema(description = "生产日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate productionDate;

    @Schema(description = "有效期至")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @Schema(description = "质检报告URL")
    private String qualityReport;

    @Schema(description = "质检状态：0-不合格，1-合格")
    private Integer qualityStatus;

    @Schema(description = "备注")
    private String remarks;
}
