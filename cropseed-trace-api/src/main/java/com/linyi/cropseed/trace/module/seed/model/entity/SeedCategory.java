package com.linyi.cropseed.trace.module.seed.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 种子品类实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seed_category")
@Schema(description = "种子品类")
public class SeedCategory extends BaseEntity {

    @Schema(description = "父品类ID")
    private Long parentId;

    @Schema(description = "品类编码")
    private String categoryCode;

    @Schema(description = "品类名称")
    private String categoryName;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "层级路径")
    private String path;

    @Schema(description = "品类描述")
    private String description;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "子品类列表")
    @TableField(exist = false)
    private List<SeedCategory> children;
}
