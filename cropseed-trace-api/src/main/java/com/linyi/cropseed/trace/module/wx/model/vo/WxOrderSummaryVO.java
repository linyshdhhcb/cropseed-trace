package com.linyi.cropseed.trace.module.wx.model.vo;

import com.linyi.cropseed.trace.module.order.model.vo.OrderItemVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 小程序订单摘要 VO
 */
@Data
public class WxOrderSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "订单状态")
    private Integer orderStatus;

    @Schema(description = "应付金额")
    private BigDecimal payableAmount;

    @Schema(description = "实付金额")
    private BigDecimal paidAmount;

    @Schema(description = "下单时间")
    private LocalDateTime createTime;

    @Schema(description = "订单商品")
    private List<OrderItemVO> items;
}
