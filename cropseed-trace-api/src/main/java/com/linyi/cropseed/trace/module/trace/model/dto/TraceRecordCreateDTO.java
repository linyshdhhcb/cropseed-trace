package com.linyi.cropseed.trace.module.trace.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建溯源记录DTO
 *
 * @author linyi
 * @since 2024-01-01
 */
@Data
@Schema(description = "创建溯源记录DTO")
public class TraceRecordCreateDTO {

    @Schema(description = "溯源码")
    @NotBlank(message = "溯源码不能为空")
    private String traceCode;

    @Schema(description = "批次ID")
    @NotNull(message = "批次ID不能为空")
    private Long batchId;

    @Schema(description = "记录类型：1-生产记录，2-质检记录，3-流通记录，4-销售记录，5-异常记录")
    @NotNull(message = "记录类型不能为空")
    private Integer recordType;

    @Schema(description = "记录阶段")
    @NotBlank(message = "记录阶段不能为空")
    private String recordStage;

    @Schema(description = "相关实体ID")
    private Long entityId;

    @Schema(description = "相关实体名称")
    private String entityName;

    @Schema(description = "操作人员")
    @NotBlank(message = "操作人员不能为空")
    private String operatorName;

    @Schema(description = "操作人电话")
    private String operatorPhone;

    @Schema(description = "记录时间")
    private LocalDateTime recordTime;

    @Schema(description = "位置信息")
    private String location;

    @Schema(description = "温度(℃)")
    private BigDecimal temperature;

    @Schema(description = "湿度(%)")
    private BigDecimal humidity;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "质量等级")
    private String qualityGrade;

    @Schema(description = "检测结果")
    private String testResult;

    @Schema(description = "内容摘要")
    @NotBlank(message = "内容摘要不能为空")
    private String contentSummary;

    @Schema(description = "详细内容(JSON格式)")
    private String detailedContent;

    @Schema(description = "图片URLs(JSON数组)")
    private String imageUrls;

    @Schema(description = "附件URLs(JSON数组)")
    private String attachmentUrls;

    @Schema(description = "备注")
    private String remark;
}
