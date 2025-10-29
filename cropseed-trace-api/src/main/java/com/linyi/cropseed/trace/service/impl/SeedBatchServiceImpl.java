package com.linyi.cropseed.trace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.entity.SeedBatch;
import com.linyi.cropseed.trace.mapper.SeedBatchMapper;
import com.linyi.cropseed.trace.mapper.SeedInfoMapper;
import com.linyi.cropseed.trace.entity.SeedInfo;
import com.linyi.cropseed.trace.service.SeedBatchService;
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
    private final SeedInfoMapper seedInfoMapper;

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

    private SeedBatchVO convertToVO(SeedBatch seedBatch) {
        SeedBatchVO vo = new SeedBatchVO();
        BeanUtils.copyProperties(seedBatch, vo);
        return vo;
    }
}
