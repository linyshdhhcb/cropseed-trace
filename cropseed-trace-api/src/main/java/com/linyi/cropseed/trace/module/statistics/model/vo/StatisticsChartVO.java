package com.linyi.cropseed.trace.module.statistics.model.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 统计图表VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class StatisticsChartVO {
    private String chartType;
    private List<Map<String, Object>> data;
    private String title;
    private String xAxis;
    private String yAxis;
}
