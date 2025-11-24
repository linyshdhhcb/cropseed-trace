package com.linyi.cropseed.trace.module.seed.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 种子品类新增DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SeedCategoryAddDTO {
    @NotBlank(message = "品类名称不能为空")
    private String categoryName;

    @NotBlank(message = "品类编码不能为空")
    private String categoryCode;

    private Long parentId;
    private String description;
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
