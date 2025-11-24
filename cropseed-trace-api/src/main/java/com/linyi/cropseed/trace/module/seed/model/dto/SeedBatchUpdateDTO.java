package com.linyi.cropseed.trace.module.seed.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 种子批次修改DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "种子批次修改DTO")
public class SeedBatchUpdateDTO {

    @Schema(description = "种子批次ID", required = true)
    @NotNull(message = "种子批次ID不能为空")
    private Long id;

    @Schema(description = "批次号", required = true)
    @NotBlank(message = "批次号不能为空")
    private String batchNo;

    @Schema(description = "种子ID", required = true)
    @NotNull(message = "种子ID不能为空")
    private Long seedId;

    @Schema(description = "生产日期", required = true)
    @NotNull(message = "生产日期不能为空")
    private LocalDate productionDate;

    @Schema(description = "有效期至")
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
    private LocalDate harvestDate;

    @Schema(description = "加工日期")
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
