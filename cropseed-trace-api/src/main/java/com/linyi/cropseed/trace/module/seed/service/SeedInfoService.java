package com.linyi.cropseed.trace.module.seed.service;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.module.seed.model.entity.SeedInfo;
import com.linyi.cropseed.trace.module.seed.model.vo.SeedInfoVO;

import java.util.List;

/**
 * 种子信息Service接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface SeedInfoService {

    /**
     * 分页查询种子信息列表
     */
    PageResult<SeedInfoVO> pageSeedInfos(PageQuery pageQuery, String seedName, String variety, Long categoryId,
            Integer status);

    /**
     * 根据ID查询种子信息详情
     */
    SeedInfoVO getSeedInfoById(Long id);

    /**
     * 新增种子信息
     */
    void addSeedInfo(SeedInfo seedInfo);

    /**
     * 修改种子信息
     */
    void updateSeedInfo(SeedInfo seedInfo);

    /**
     * 删除种子信息
     */
    void deleteSeedInfo(Long id);

    /**
     * 批量删除种子信息
     */
    void batchDeleteSeedInfos(List<Long> ids);
}
