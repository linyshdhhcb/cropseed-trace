package com.linyi.cropseed.trace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.entity.SeedBatch;
import com.linyi.cropseed.trace.entity.TraceRecord;
import com.linyi.cropseed.trace.mapper.SeedBatchMapper;
import com.linyi.cropseed.trace.mapper.SeedInfoMapper;
import com.linyi.cropseed.trace.entity.SeedInfo;
import com.linyi.cropseed.trace.service.SeedBatchService;
import com.linyi.cropseed.trace.service.TraceRecordService;
import com.linyi.cropseed.trace.vo.SeedBatchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 种子批次Service实现类
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeedBatchServiceImpl implements SeedBatchService {

    private final SeedBatchMapper seedBatchMapper;
    private final TraceRecordService traceRecordService;

    @Override
    public PageResult<SeedBatchVO> pageSeedBatches(PageQuery pageQuery, String batchNo, Long seedId) {
        Page<SeedBatchVO> voPage = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        voPage = seedBatchMapper.selectPageVo(voPage, batchNo, seedId);
        return PageResult.of(voPage, voPage.getRecords());
    }

    @Override
    public SeedBatchVO getSeedBatchById(Long id) {
        return seedBatchMapper.selectDetailById(id);
    }

    @Override
    @Transactional
    public void addSeedBatch(SeedBatch seedBatch) {
        seedBatchMapper.insert(seedBatch);
        log.info("新增种子批次成功: {}", seedBatch.getBatchNo());
    }

    @Override
    @Transactional
    public void updateSeedBatch(SeedBatch seedBatch) {
        seedBatchMapper.updateById(seedBatch);
        log.info("修改种子批次成功: {}", seedBatch.getBatchNo());
    }

    @Override
    @Transactional
    public void deleteSeedBatch(Long id) {
        seedBatchMapper.deleteById(id);
        log.info("删除种子批次成功: {}", id);
    }

    @Override
    @Transactional
    public void batchDeleteSeedBatches(List<Long> ids) {
        seedBatchMapper.deleteBatchIds(ids);
        log.info("批量删除种子批次成功: {}", ids);
    }

    @Override
    @Transactional
    public void updateBatchTraceCode(Long id, String traceCode) {
        // 更新批次的溯源码
        SeedBatch seedBatch = new SeedBatch();
        seedBatch.setId(id);
        seedBatch.setTraceCode(traceCode);
        seedBatchMapper.updateById(seedBatch);
        log.info("更新批次溯源码成功: batchId={}, traceCode={}", id, traceCode);

        //  查询批次详情，用于创建溯源记录
        SeedBatch batch = seedBatchMapper.selectById(id);
        if (batch == null) {
            log.error("批次不存在: batchId={}", id);
            throw new RuntimeException("批次不存在");
        }

        //  创建初始溯源记录
        TraceRecord traceRecord = new TraceRecord();
        traceRecord.setTraceCode(traceCode);
        traceRecord.setBatchId(id);
        traceRecord.setRecordType(1); // 1-生产记录
        traceRecord.setRecordStage("批次创建");
        traceRecord.setOperatorName("系统管理员");
        traceRecord.setRecordTime(batch.getProductionDate() != null ? batch.getProductionDate().atStartOfDay() : java.time.LocalDateTime.now());
        traceRecord.setContentSummary("批次 " + batch.getBatchNo() + " 生成溯源码");

        // 构建详细内容
        String detailedContent = String.format(
            "{\"action\":\"生成溯源码\",\"batchNo\":\"%s\",\"traceCode\":\"%s\",\"productionDate\":\"%s\"}",
            batch.getBatchNo(), traceCode, batch.getProductionDate()
        );
        traceRecord.setDetailedContent(detailedContent);
        traceRecord.setBlockchainStatus(0); // 0-未上链

        try {
            traceRecordService.createTraceRecord(traceRecord);
            log.info("创建初始溯源记录成功: traceCode={}", traceCode);
        } catch (Exception e) {
            log.error("创建初始溯源记录失败: traceCode={}", traceCode, e);
            // 不影响主流程，只记录日志
        }
    }
}
