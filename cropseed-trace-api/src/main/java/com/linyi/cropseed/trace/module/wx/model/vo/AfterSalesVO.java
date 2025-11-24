package com.linyi.cropseed.trace.module.wx.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 售后 VO
 */
@Data
public class AfterSalesVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "售后ID")
    private Long id;

    @Schema(description = "售后单号")
    private String afterSalesNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "售后类型")
    private Integer type;

    @Schema(description = "售后原因")
    private String reason;

    @Schema(description = "凭证图片")
    private List<String> evidenceImages;

    @Schema(description = "售后状态")
    private Integer status;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "处理备注")
    private String processRemark;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;

    @Schema(description = "处理时间")
    private LocalDateTime processTime;
}
