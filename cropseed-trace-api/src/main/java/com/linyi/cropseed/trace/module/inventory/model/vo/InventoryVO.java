package com.linyi.cropseed.trace.module.inventory.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "库存VO")
public class InventoryVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "种子ID")
    private Long seedId;

    @Schema(description = "种子名称")
    private String seedName;

    @Schema(description = "种子编码")
    private String seedCode;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "仓库名称")
    private String warehouseName;

    @Schema(description = "仓库编码")
    private String warehouseCode;

    @Schema(description = "库存数量")
    private Integer quantity;

    @Schema(description = "锁定数量")
    private Integer lockedQuantity;

    @Schema(description = "可用数量")
    private Integer availableQuantity;

    @Schema(description = "最低库存预警")
    private Integer minStock;

    @Schema(description = "最高库存预警")
    private Integer maxStock;
}
