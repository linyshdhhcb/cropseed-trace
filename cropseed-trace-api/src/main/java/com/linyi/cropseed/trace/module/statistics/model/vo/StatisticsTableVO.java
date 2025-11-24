package com.linyi.cropseed.trace.module.statistics.model.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 统计表格VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class StatisticsTableVO {
    private String tableType;
    private List<Map<String, Object>> data;
    private List<String> columns;
    private String title;
}
