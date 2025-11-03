package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.linyi.cropseed.trace.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 入库记录实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory_inbound")
public class InventoryInbound extends BaseEntity {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "入库记录ID")
    private Long id;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "种子批次ID")
    private Long seedBatchId;

    @Schema(description = "入库数量")
    private Integer quantity;

    @Schema(description = "供应商")
    private String supplier;

    @Schema(description = "质量等级")
    private String qualityGrade;

    @Schema(description = "存储条件")
    private String storageCondition;

    @Schema(description = "入库状态：0-待审核，1-待入库，2-入库完成")
    private Integer status;

    @Schema(description = "入库备注")
    private String remark;
}
