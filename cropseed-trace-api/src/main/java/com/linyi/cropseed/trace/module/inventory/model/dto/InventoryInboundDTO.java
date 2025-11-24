package com.linyi.cropseed.trace.module.inventory.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 入库DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "入库DTO")
public class InventoryInboundDTO {

    @Schema(description = "批次号", required = true)
    @NotNull(message = "批次号不能为空")
    private String batchNo;

    @Schema(description = "种子批次ID", required = true)
    @NotNull(message = "种子批次ID不能为空")
    private Long seedBatchId;

    @Schema(description = "仓库ID", required = true)
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "入库数量", required = true)
    @NotNull(message = "入库数量不能为空")
    @Positive(message = "入库数量必须大于0")
    private Integer quantity;

    @Schema(description = "供应商")
    @Size(max = 255, message = "供应商长度不能超过255个字符")
    private String supplier;

    @Schema(description = "质量等级")
    @Size(max = 255, message = "质量等级长度不能超过255个字符")
    private String qualityGrade;

    @Schema(description = "存储条件")
    @Size(max = 255, message = "存储条件长度不能超过255个字符")
    private String storageCondition;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;
}
