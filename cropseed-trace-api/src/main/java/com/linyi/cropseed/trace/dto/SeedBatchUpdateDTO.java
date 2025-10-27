package com.linyi.cropseed.trace.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 种子批次修改DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class SeedBatchUpdateDTO {
    @NotNull(message = "种子批次ID不能为空")
    private Long id;

    @NotBlank(message = "批次号不能为空")
    private String batchNo;

    @NotNull(message = "种子ID不能为空")
    private Long seedId;

    private LocalDateTime productionDate;
    private LocalDateTime expiryDate;
    private String qualityReport;
    private Integer qualityStatus;
}
