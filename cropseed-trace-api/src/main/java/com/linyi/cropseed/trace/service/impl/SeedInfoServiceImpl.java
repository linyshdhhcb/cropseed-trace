package com.linyi.cropseed.trace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.entity.SeedInfo;
import com.linyi.cropseed.trace.mapper.SeedInfoMapper;
import com.linyi.cropseed.trace.service.SeedInfoService;
import com.linyi.cropseed.trace.vo.SeedInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 种子信息Service实现类
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeedInfoServiceImpl implements SeedInfoService {

    private final SeedInfoMapper seedInfoMapper;

    @Override
    public PageResult<SeedInfoVO> pageSeedInfos(PageQuery pageQuery, String seedName, String variety, Long categoryId,
            Integer status) {
        // 使用PageQuery的排序功能
        Page<SeedInfoVO> voPage = pageQuery.toMpPageWithOrder();
        voPage = seedInfoMapper.selectPageVo(voPage, seedName, variety, categoryId, status);
        return PageResult.of(voPage, voPage.getRecords());
    }

    @Override
    public SeedInfoVO getSeedInfoById(Long id) {
        return seedInfoMapper.selectDetailById(id);
    }

    @Override
    @Transactional
    public void addSeedInfo(SeedInfo seedInfo) {
        seedInfoMapper.insert(seedInfo);
        log.info("新增种子信息成功: {}", seedInfo.getSeedName());
    }

    @Override
    @Transactional
    public void updateSeedInfo(SeedInfo seedInfo) {
        seedInfoMapper.updateById(seedInfo);
        log.info("修改种子信息成功: {}", seedInfo.getSeedName());
    }

    @Override
    @Transactional
    public void deleteSeedInfo(Long id) {
        seedInfoMapper.deleteById(id);
        log.info("删除种子信息成功: {}", id);
    }

    @Override
    @Transactional
    public void batchDeleteSeedInfos(List<Long> ids) {
        seedInfoMapper.deleteBatchIds(ids);
        log.info("批量删除种子信息成功: {}", ids);
    }

    private SeedInfoVO convertToVO(SeedInfo seedInfo) {
        SeedInfoVO vo = new SeedInfoVO();
        BeanUtils.copyProperties(seedInfo, vo);
        return vo;
    }
}
