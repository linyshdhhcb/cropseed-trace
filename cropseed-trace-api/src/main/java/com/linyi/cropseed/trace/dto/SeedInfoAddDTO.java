package com.linyi.cropseed.trace.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 种子信息新增DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SeedInfoAddDTO {
    @NotBlank(message = "种子名称不能为空")
    private String seedName;

    @NotBlank(message = "种子编码不能为空")
    private String seedCode;

    @NotNull(message = "品类ID不能为空")
    private Long categoryId;

    private String variety;
    private String originPlace;
    private String characteristics;
    private String specifications;
    private String imageUrl;
    private java.math.BigDecimal unitPrice;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
