package com.linyi.cropseed.trace.module.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单操作日志实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_operation_log")
@Schema(description = "订单操作日志")
public class OrderOperationLog extends BaseEntity {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作类型")
    private String operation;

    @Schema(description = "原状态")
    private Integer fromStatus;

    @Schema(description = "新状态")
    private Integer toStatus;

    @Schema(description = "操作备注")
    private String remark;
}
