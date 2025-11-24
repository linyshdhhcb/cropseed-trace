package com.linyi.cropseed.trace.module.inventory.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
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
@TableName("inbound_record")
@Schema(description = "入库记录")
public class InboundRecord extends BaseEntity {

    @Schema(description = "入库单号")
    private String inboundNo;

    @Schema(description = "种子ID")
    private Long seedId;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "入库数量")
    private Integer quantity;

    @Schema(description = "入库时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inboundTime;

    @Schema(description = "操作员ID")
    private Long operatorId;

    @Schema(description = "备注")
    private String remarks;
}
