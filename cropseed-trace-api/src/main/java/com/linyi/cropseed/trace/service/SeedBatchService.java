package com.linyi.cropseed.trace.service;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.entity.SeedBatch;
import com.linyi.cropseed.trace.vo.SeedBatchVO;

import java.util.List;

/**
 * 种子批次Service接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface SeedBatchService {

    /**
     * 分页查询种子批次列表
     */
    PageResult<SeedBatchVO> pageSeedBatches(PageQuery pageQuery, String batchNo, Long seedId);

    /**
     * 根据ID查询种子批次详情
     */
    SeedBatchVO getSeedBatchById(Long id);

    /**
     * 新增种子批次
     */
    void addSeedBatch(SeedBatch seedBatch);

    /**
     * 修改种子批次
     */
    void updateSeedBatch(SeedBatch seedBatch);

    /**
     * 删除种子批次
     */
    void deleteSeedBatch(Long id);

    /**
     * 批量删除种子批次
     */
    void batchDeleteSeedBatches(List<Long> ids);
}
