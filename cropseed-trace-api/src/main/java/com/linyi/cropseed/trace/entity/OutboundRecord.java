package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.linyi.cropseed.trace.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 出库记录实体
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("outbound_record")
@Schema(description = "出库记录")
public class OutboundRecord extends BaseEntity {

    @Schema(description = "出库单号")
    private String outboundNo;

    @Schema(description = "种子ID")
    private Long seedId;

    @Schema(description = "批次ID")
    private Long batchId;

    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "出库数量")
    private Integer quantity;

    @Schema(description = "出库时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outboundTime;

    @Schema(description = "操作员ID")
    private Long operatorId;

    @Schema(description = "出库用途")
    private String purpose;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "备注")
    private String remarks;
}
