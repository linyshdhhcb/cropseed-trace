package com.linyi.cropseed.trace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户行为上报 DTO
 */
@Data
public class UserBehaviorReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "行为类型：1-浏览，2-点击，3-加入购物车，4-购买，5-收藏")
    @NotNull(message = "行为类型不能为空")
    private Integer behaviorType;

    @Schema(description = "目标商品ID")
    @NotNull(message = "目标ID不能为空")
    private Long seedId;

    @Schema(description = "行为来源")
    private String source;

    @Schema(description = "停留时长，单位秒")
    private Integer duration;
}
