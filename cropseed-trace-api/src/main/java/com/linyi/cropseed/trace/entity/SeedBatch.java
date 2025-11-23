package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
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

    @Schema(description = "溯源码")
    private String traceCode;

    @Schema(description = "生产商ID")
    private Long producerId;

    @Schema(description = "生产商名称")
    private String producerName;

    @Schema(description = "生产地点")
    private String productionLocation;

    @Schema(description = "收获日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate harvestDate;

    @Schema(description = "加工日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate processingDate;

    @Schema(description = "储存条件")
    private String storageCondition;

    @Schema(description = "认证信息(有机/绿色/无公害等)")
    private String certification;

    @Schema(description = "可追溯等级：1-基础，2-详细，3-完整")
    private Integer traceabilityLevel;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "初始质量等级")
    private String initialQualityGrade;

    @Schema(description = "含水率(%)")
    private BigDecimal moistureContent;

    @Schema(description = "发芽率(%)")
    private BigDecimal germinationRate;

    @Schema(description = "纯度(%)")
    private BigDecimal purity;

    @Schema(description = "包装类型")
    private String packagingType;

    @Schema(description = "包装规格")
    private String packagingSpecification;

    @Schema(description = "初始操作员姓名")
    private String initialOperatorName;

    @Schema(description = "初始操作员电话")
    private String initialOperatorPhone;

    @Schema(description = "生产设备")
    private String productionEquipment;

    @Schema(description = "加工方式")
    private String processingMethod;

    @Schema(description = "种子来源")
    private String seedSource;

    @Schema(description = "亲本品种")
    private String parentVariety;
}
