package com.linyi.cropseed.trace.module.inventory.model.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 库存导入DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "库存导入DTO")
public class InventoryImportDTO {

    @ExcelProperty("种子编码")
    @Schema(description = "种子编码")
    private String seedCode;

    @ExcelProperty("仓库编码")
    @Schema(description = "仓库编码")
    private String warehouseCode;

    @ExcelProperty("库存数量")
    @Schema(description = "库存数量")
    private Integer quantity;

    @ExcelProperty("最低库存")
    @Schema(description = "最低库存")
    private Integer minStock;

    @ExcelProperty("最高库存")
    @Schema(description = "最高库存")
    private Integer maxStock;
}
