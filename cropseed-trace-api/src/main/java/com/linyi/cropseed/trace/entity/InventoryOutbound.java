package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出库记录实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory_outbound")
public class InventoryOutbound extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "批次ID")
    private String batchNo;

    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "种子批次ID")
    private Long seedBatchId;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "收件人")
    private String recipient;

    @Schema(description = "目的")
    private String purpose;

    @Schema(description = "状态：0-待审核，1-待出库，2-已出库，3-已取消")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
