package com.linyi.cropseed.trace.module.trace.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.trace.model.dto.TraceRecordCreateDTO;
import com.linyi.cropseed.trace.module.trace.model.vo.BlockchainProof;
import com.linyi.cropseed.trace.module.trace.model.entity.TraceRecord;
import com.linyi.cropseed.trace.module.trace.service.TraceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 溯源记录管理接口
 *
 * @author linyi
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/trace/records")
@RequiredArgsConstructor
@Tag(name = "溯源记录管理", description = "溯源记录增删改查及区块链相关操作")
public class TraceRecordController {

    private final TraceRecordService traceRecordService;

    @Operation(summary = "创建溯源记录", description = "创建新的溯源记录并自动上链")
    @PostMapping
    public Result<Long> createTraceRecord(@Valid @RequestBody TraceRecordCreateDTO createDTO) {
        try {
            TraceRecord traceRecord = new TraceRecord();
            BeanUtils.copyProperties(createDTO, traceRecord);

            if (createDTO.getRecordTime() == null) {
                traceRecord.setRecordTime(LocalDateTime.now());
            }

            //  创建记录（包含区块链上链）
            Long recordId = traceRecordService.createTraceRecord(traceRecord);

            log.info("创建溯源记录成功: recordId={}, traceCode={}", recordId, createDTO.getTraceCode());
            return Result.success("创建溯源记录成功", recordId);

        } catch (Exception e) {
            log.error("创建溯源记录失败: traceCode={}", createDTO.getTraceCode(), e);
            return Result.fail("创建溯源记录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取溯源链", description = "根据溯源码查询完整的溯源链信息")
    @GetMapping("/chain/{traceCode}")
    public Result<List<TraceRecord>> getTraceChain(@Parameter(description = "溯源码") @PathVariable String traceCode) {
        try {
            List<TraceRecord> traceChain = traceRecordService.getTraceChain(traceCode);
            log.info("查询溯源链成功: traceCode={}, recordCount={}", traceCode, traceChain.size());
            return Result.success(traceChain);
        } catch (Exception e) {
            log.error("查询溯源链失败: traceCode={}", traceCode, e);
            return Result.fail("查询溯源链失败: " + e.getMessage());
        }
    }

    @Operation(summary = "分页查询溯源记录", description = "根据条件分页查询溯源记录")
    @GetMapping("/page")
    public Result<PageResult<TraceRecord>> getTraceRecordsPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") Long pageSize,
            @Parameter(description = "溯源码") @RequestParam(required = false) String traceCode,
            @Parameter(description = "批次ID") @RequestParam(required = false) Long batchId,
            @Parameter(description = "记录类型") @RequestParam(required = false) Integer recordType) {
        try {
            Page<TraceRecord> page = new Page<>(pageNum, pageSize);
            Page<TraceRecord> result = traceRecordService.getTraceRecordsPage(page, traceCode, batchId, recordType);

            PageResult<TraceRecord> pageResult = PageResult.of(result);
            log.info("分页查询溯源记录成功: pageNum={}, pageSize={}, total={}", pageNum, pageSize, pageResult.getTotal());

            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("分页查询溯源记录失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据ID查询记录", description = "根据记录ID查询详细信息")
    @GetMapping("/{id}")
    public Result<TraceRecord> getTraceRecordById(@Parameter(description = "记录ID") @PathVariable Long id) {
        try {
            TraceRecord traceRecord = traceRecordService.getById(id);
            if (traceRecord == null) {
                return Result.fail("溯源记录不存在");
            }
            return Result.success(traceRecord);
        } catch (Exception e) {
            log.error("查询溯源记录失败: id={}", id, e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Operation(summary = "验证溯源数据完整性", description = "验证本地数据与区块链数据的一致性")
    @PostMapping("/verify/{traceCode}")
    public Result<Boolean> verifyTraceIntegrity(@Parameter(description = "溯源码") @PathVariable String traceCode) {
        try {
            boolean verified = traceRecordService.verifyTraceIntegrity(traceCode);
            String message = verified ? "数据验证通过" : "数据验证失败，可能存在篡改";
            log.info("溯源数据验证: traceCode={}, verified={}", traceCode, verified);
            return Result.success(message, verified);
        } catch (Exception e) {
            log.error("溯源数据验证异常: traceCode={}", traceCode, e);
            return Result.fail("验证失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取区块链证明", description = "生成溯源数据的区块链存在证明")
    @GetMapping("/proof/{traceCode}")
    public Result<BlockchainProof> getTraceProof(@Parameter(description = "溯源码") @PathVariable String traceCode) {
        try {
            BlockchainProof proof = traceRecordService.getTraceProof(traceCode);
            log.info("生成区块链证明成功: traceCode={}, txHash={}", traceCode, proof.getTxHash());
            return Result.success("区块链证明生成成功", proof);
        } catch (Exception e) {
            log.error("生成区块链证明失败: traceCode={}", traceCode, e);
            return Result.fail("生成证明失败: " + e.getMessage());
        }
    }

    @Operation(summary = "单个记录上链", description = "将指定记录上传到区块链")
    @PostMapping("/{id}/upload")
    public Result<Void> uploadSingleRecord(@Parameter(description = "记录ID") @PathVariable Long id) {
        try {
            traceRecordService.uploadSingleRecordToBlockchain(id);
            log.info("记录上链请求已提交: recordId={}", id);
            return Result.success("上链请求已提交，请稍后查看上链状态");
        } catch (Exception e) {
            log.error("记录上链失败: recordId={}", id, e);
            return Result.fail("上链失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量上链", description = "将未上链的记录批量上传到区块链")
    @PostMapping("/batch-upload")
    public Result<Integer> batchUploadToBlockchain(@Parameter(description = "处理数量限制") @RequestParam(defaultValue = "100") Integer limit) {
        try {
            int successCount = traceRecordService.batchUploadToBlockchain(limit);
            log.info("批量上链完成: successCount={}", successCount);
            return Result.success("批量上链完成", successCount);
        } catch (Exception e) {
            log.error("批量上链失败", e);
            return Result.fail("批量上链失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新记录", description = "更新溯源记录信息")
    @PutMapping("/{id}")
    public Result<Boolean> updateTraceRecord(
            @Parameter(description = "记录ID") @PathVariable Long id,
            @Valid @RequestBody TraceRecordCreateDTO updateDTO) {
        try {
            TraceRecord traceRecord = new TraceRecord();
            BeanUtils.copyProperties(updateDTO, traceRecord);
            traceRecord.setId(id);

            boolean updated = traceRecordService.updateById(traceRecord);
            if (updated) {
                log.info("更新溯源记录成功: id={}", id);
                return Result.success("更新成功", true);
            } else {
                return Result.fail("记录不存在或更新失败");
            }
        } catch (Exception e) {
            log.error("更新溯源记录失败: id={}", id, e);
            return Result.fail("更新失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除记录", description = "删除溯源记录（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteTraceRecord(@Parameter(description = "记录ID") @PathVariable Long id) {
        try {
            boolean deleted = traceRecordService.removeById(id);
            if (deleted) {
                log.info("删除溯源记录成功: id={}", id);
                return Result.success("删除成功", true);
            } else {
                return Result.fail("记录不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除溯源记录失败: id={}", id, e);
            return Result.fail("删除失败: " + e.getMessage());
        }
    }
}
