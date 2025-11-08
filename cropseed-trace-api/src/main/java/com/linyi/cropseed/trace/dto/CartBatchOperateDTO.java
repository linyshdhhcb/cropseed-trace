package com.linyi.cropseed.trace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车批量操作 DTO
 */
@Data
public class CartBatchOperateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "购物车ID集合")
    @NotEmpty(message = "购物车ID集合不能为空")
    private List<Long> cartIds;

    @Schema(description = "选中状态")
    private Boolean selected;
}
