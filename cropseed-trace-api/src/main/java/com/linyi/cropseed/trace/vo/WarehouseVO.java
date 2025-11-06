package com.linyi.cropseed.trace.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 仓库VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Schema(description = "仓库VO")
public class WarehouseVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "仓库编码")
    private String warehouseCode;

    @Schema(description = "仓库名称")
    private String warehouseName;

    @Schema(description = "仓库位置")
    private String location;

    @Schema(description = "负责人")
    private String manager;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "仓库容量")
    private BigDecimal capacity;

    @Schema(description = "已用容量")
    private BigDecimal usedCapacity;

    @Schema(description = "容量使用率(%)")
    private BigDecimal usageRate;

    @Schema(description = "状态：0-停用，1-启用")
    private Integer status;

    @Schema(description = "仓库类型（如：普通仓、冷藏仓、保税仓等）")
    private String warehouseType;

    @Schema(description = "温度范围（如：-18℃~-10℃、常温）")
    private String temperatureRange;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String createTime;
}
