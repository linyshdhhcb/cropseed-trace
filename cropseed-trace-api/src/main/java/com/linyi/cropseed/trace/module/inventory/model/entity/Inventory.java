package com.linyi.cropseed.trace.module.inventory.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存台账实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory")
@Schema(description = "库存台账")
public class Inventory extends BaseEntity {

    @Schema(description = "种子ID")
    private Long seedId;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "仓库ID")
    private Long warehouseId;

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
