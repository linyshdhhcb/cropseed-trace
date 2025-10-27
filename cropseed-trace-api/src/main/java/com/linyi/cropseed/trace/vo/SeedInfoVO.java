package com.linyi.cropseed.trace.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 种子信息VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SeedInfoVO {
    private Long id;
    private String seedName;
    private String variety;
    private Long categoryId;
    private String categoryName;
    private String description;
    private String origin;
    private String plantingSeason;
    private Integer maturityDays;
    private String yield;
    private String diseaseResistance;
    private String pestResistance;
    private String droughtResistance;
    private String coldResistance;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
