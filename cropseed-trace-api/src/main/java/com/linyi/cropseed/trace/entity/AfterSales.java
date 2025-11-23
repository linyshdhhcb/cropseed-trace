package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("after_sales")
public class AfterSales extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String afterSalesNo;

    private Long orderId;

    private Long userId;

    private Integer type;

    private String reason;

    private String evidenceImages;

    private Integer status;

    private BigDecimal refundAmount;

    private Long processorId;

    private String processRemark;

    private LocalDateTime processTime;
}
