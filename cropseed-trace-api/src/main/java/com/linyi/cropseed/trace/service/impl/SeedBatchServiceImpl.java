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

        // 查询批次详情，用于创建溯源记录
        SeedBatch batch = seedBatchMapper.selectById(id);
        if (batch == null) {
            log.error("批次不存在: batchId={}", id);
            throw new RuntimeException("批次不存在");
        }

        // 创建初始溯源记录 - 将seed_batch的所有关键信息同步到trace_record
        TraceRecord traceRecord = new TraceRecord();
        
        // 基础信息
        traceRecord.setTraceCode(traceCode);
        traceRecord.setBatchId(id);
        traceRecord.setRecordType(1); // 1-生产记录
        traceRecord.setRecordStage("批次创建-生产记录");
        traceRecord.setRecordTime(batch.getProductionDate() != null ? 
            batch.getProductionDate().atStartOfDay() : java.time.LocalDateTime.now());
        
        // 实体信息（生产商）
        if (batch.getProducerId() != null) {
            traceRecord.setEntityId(batch.getProducerId());
        }
        traceRecord.setEntityName(batch.getProducerName());
        
        // 操作员信息（使用批次的初始操作员）
        traceRecord.setOperatorName(batch.getInitialOperatorName() != null ? 
            batch.getInitialOperatorName() : "系统管理员");
        traceRecord.setOperatorPhone(batch.getInitialOperatorPhone());
        
        // 位置信息
        traceRecord.setLocation(batch.getProductionLocation());
        
        // 单位和数量（初始记录不填数量，后续流通时填写）
        traceRecord.setUnit(batch.getUnit());
        
        // 质量等级
        traceRecord.setQualityGrade(batch.getInitialQualityGrade());
        
        // 内容摘要
        traceRecord.setContentSummary(String.format(
            "批次 %s 生成溯源码，生产商：%s，生产地点：%s，质量等级：%s",
            batch.getBatchNo(),
            batch.getProducerName() != null ? batch.getProducerName() : "未填写",
            batch.getProductionLocation() != null ? batch.getProductionLocation() : "未填写",
            batch.getInitialQualityGrade() != null ? batch.getInitialQualityGrade() : "未评级"
        ));

        // 构建详细内容（JSON格式，包含所有关键信息）
        StringBuilder detailBuilder = new StringBuilder();
        detailBuilder.append("{");
        detailBuilder.append("\"action\":\"批次创建-生成溯源码\",");
        detailBuilder.append("\"batchNo\":\"").append(batch.getBatchNo()).append("\",");
        detailBuilder.append("\"traceCode\":\"").append(traceCode).append("\",");
        detailBuilder.append("\"seedId\":").append(batch.getSeedId()).append(",");
        
        // 生产信息
        if (batch.getProducerName() != null) {
            detailBuilder.append("\"producerName\":\"").append(batch.getProducerName()).append("\",");
        }
        if (batch.getProductionLocation() != null) {
            detailBuilder.append("\"productionLocation\":\"").append(batch.getProductionLocation()).append("\",");
        }
        if (batch.getProductionDate() != null) {
            detailBuilder.append("\"productionDate\":\"").append(batch.getProductionDate()).append("\",");
        }
        if (batch.getHarvestDate() != null) {
            detailBuilder.append("\"harvestDate\":\"").append(batch.getHarvestDate()).append("\",");
        }
        if (batch.getProcessingDate() != null) {
            detailBuilder.append("\"processingDate\":\"").append(batch.getProcessingDate()).append("\",");
        }
        if (batch.getProductionEquipment() != null) {
            detailBuilder.append("\"productionEquipment\":\"").append(batch.getProductionEquipment()).append("\",");
        }
        if (batch.getProcessingMethod() != null) {
            detailBuilder.append("\"processingMethod\":\"").append(batch.getProcessingMethod()).append("\",");
        }
        if (batch.getSeedSource() != null) {
            detailBuilder.append("\"seedSource\":\"").append(batch.getSeedSource()).append("\",");
        }
        if (batch.getParentVariety() != null) {
            detailBuilder.append("\"parentVariety\":\"").append(batch.getParentVariety()).append("\",");
        }
        
        // 质量信息
        if (batch.getInitialQualityGrade() != null) {
            detailBuilder.append("\"qualityGrade\":\"").append(batch.getInitialQualityGrade()).append("\",");
        }
        if (batch.getMoistureContent() != null) {
            detailBuilder.append("\"moistureContent\":").append(batch.getMoistureContent()).append(",");
        }
        if (batch.getGerminationRate() != null) {
            detailBuilder.append("\"germinationRate\":").append(batch.getGerminationRate()).append(",");
        }
        if (batch.getPurity() != null) {
            detailBuilder.append("\"purity\":").append(batch.getPurity()).append(",");
        }
        detailBuilder.append("\"qualityStatus\":").append(batch.getQualityStatus()).append(",");
        
        // 包装和储存
        if (batch.getUnit() != null) {
            detailBuilder.append("\"unit\":\"").append(batch.getUnit()).append("\",");
        }
        if (batch.getPackagingType() != null) {
            detailBuilder.append("\"packagingType\":\"").append(batch.getPackagingType()).append("\",");
        }
        if (batch.getPackagingSpecification() != null) {
            detailBuilder.append("\"packagingSpecification\":\"").append(batch.getPackagingSpecification()).append("\",");
        }
        if (batch.getStorageCondition() != null) {
            detailBuilder.append("\"storageCondition\":\"").append(batch.getStorageCondition()).append("\",");
        }
        
        // 认证信息
        if (batch.getCertification() != null) {
            detailBuilder.append("\"certification\":\"").append(batch.getCertification()).append("\",");
        }
        if (batch.getTraceabilityLevel() != null) {
            detailBuilder.append("\"traceabilityLevel\":").append(batch.getTraceabilityLevel()).append(",");
        }
        
        // 操作员信息
        if (batch.getInitialOperatorName() != null) {
            detailBuilder.append("\"operatorName\":\"").append(batch.getInitialOperatorName()).append("\",");
        }
        if (batch.getInitialOperatorPhone() != null) {
            detailBuilder.append("\"operatorPhone\":\"").append(batch.getInitialOperatorPhone()).append("\",");
        }
        
        // 移除最后一个逗号并关闭JSON
        String jsonContent = detailBuilder.toString();
        if (jsonContent.endsWith(",")) {
            jsonContent = jsonContent.substring(0, jsonContent.length() - 1);
        }
        jsonContent += "}";
        
        traceRecord.setDetailedContent(jsonContent);
        traceRecord.setBlockchainStatus(0); // 0-未上链

        try {
            traceRecordService.createTraceRecord(traceRecord);
            log.info("创建初始溯源记录成功: traceCode={}, 已同步seed_batch所有关键信息", traceCode);
        } catch (Exception e) {
            log.error("创建初始溯源记录失败: traceCode={}", traceCode, e);
            throw new RuntimeException("创建初始溯源记录失败: " + e.getMessage());
        }
    }
}
