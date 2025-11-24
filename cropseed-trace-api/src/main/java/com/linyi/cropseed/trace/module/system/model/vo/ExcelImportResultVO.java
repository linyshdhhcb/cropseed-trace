package com.linyi.cropseed.trace.module.system.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Excel导入结果VO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@Builder
@Schema(description = "Excel导入结果VO")
public class ExcelImportResultVO {

    @Schema(description = "总记录数")
    private Integer totalCount;

    @Schema(description = "成功记录数")
    private Integer successCount;

    @Schema(description = "失败记录数")
    private Integer errorCount;

    @Schema(description = "错误详情列表")
    private List<Map<String, Object>> errorList;
}
