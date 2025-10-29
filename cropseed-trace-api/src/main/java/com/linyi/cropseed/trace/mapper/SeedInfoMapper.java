package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.entity.SeedInfo;
import com.linyi.cropseed.trace.vo.SeedInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 种子信息Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface SeedInfoMapper extends BaseMapper<SeedInfo> {

    /**
     * 根据品类ID查询种子列表
     */
    List<SeedInfo> selectByCategoryId(Long categoryId);

    /**
     * 根据种子编码查询种子
     */
    SeedInfo selectBySeedCode(String seedCode);

    /**
     * 根据种子名称和品类ID查询种子
     */
    SeedInfo selectBySeedNameAndCategoryId(String seedName, Long categoryId);

    /**
     * 分页联表查询种子档案（带品类名称）
     */
    Page<SeedInfoVO> selectPageVo(Page<SeedInfoVO> page,
            @Param("seedName") String seedName,
            @Param("variety") String variety,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status);

    /**
     * 联表查询详情
     */
    SeedInfoVO selectDetailById(@Param("id") Long id);
}
