package com.linyi.cropseed.trace.module.seed.model.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 种子导入DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "种子导入DTO")
public class SeedImportDTO {

    @ExcelProperty("种子名称")
    @Schema(description = "种子名称")
    private String seedName;

    @ExcelProperty("种子编码")
    @Schema(description = "种子编码")
    private String seedCode;

    @ExcelProperty("品类编码")
    @Schema(description = "品类编码")
    private String categoryCode;

    @ExcelProperty("品种")
    @Schema(description = "品种")
    private String variety;

    @ExcelProperty("产地")
    @Schema(description = "产地")
    private String originPlace;

    @ExcelProperty("特性描述")
    @Schema(description = "特性描述")
    private String characteristics;

    @ExcelProperty("规格参数")
    @Schema(description = "规格参数")
    private String specifications;

    @ExcelProperty("单价")
    @Schema(description = "单价")
    private BigDecimal unitPrice;
}
