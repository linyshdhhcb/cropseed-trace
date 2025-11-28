package com.linyi.cropseed.trace.module.system.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 操作日志批量删除DTO
 *
 * @author LinYi
 * @since 2025-11-28
 */
@Data
@Schema(description = "操作日志批量删除DTO")
public class SysOperationLogBatchDeleteDTO {

    @Schema(description = "日志ID列表", required = true)
    @NotEmpty(message = "日志ID列表不能为空")
    private List<Long> ids;
}
