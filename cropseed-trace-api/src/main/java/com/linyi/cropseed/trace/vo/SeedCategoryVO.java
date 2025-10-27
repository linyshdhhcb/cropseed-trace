package com.linyi.cropseed.trace.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 种子品类VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SeedCategoryVO {
    private Long id;
    private String categoryName;
    private String categoryCode;
    private Long parentId;
    private String parentName;
    private String description;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
