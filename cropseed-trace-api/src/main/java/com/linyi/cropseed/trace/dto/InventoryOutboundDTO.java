package com.linyi.cropseed.trace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 出库DTO
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "出库DTO")
public class InventoryOutboundDTO {

    @Schema(description = "种子ID", required = true)
    @NotNull(message = "种子ID不能为空")
    private Long seedId;

    @Schema(description = "批次ID", required = true)
    @NotNull(message = "批次ID不能为空")
    private Long batchId;

    @Schema(description = "仓库ID", required = true)
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "出库数量", required = true)
    @NotNull(message = "出库数量不能为空")
    @Positive(message = "出库数量必须大于0")
    private Integer quantity;

    @Schema(description = "出库用途")
    @Size(max = 200, message = "出库用途长度不能超过200个字符")
    private String purpose;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remarks;
}
