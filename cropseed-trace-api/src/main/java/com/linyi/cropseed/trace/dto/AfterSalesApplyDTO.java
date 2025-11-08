package com.linyi.cropseed.trace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 售后申请 DTO
 */
@Data
public class AfterSalesApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "售后类型：1-退货，2-换货")
    @NotNull(message = "售后类型不能为空")
    private Integer type;

    @Schema(description = "售后原因")
    @NotEmpty(message = "请填写售后原因")
    private String reason;

    @Schema(description = "凭证图片")
    private List<String> evidenceImages;
}
