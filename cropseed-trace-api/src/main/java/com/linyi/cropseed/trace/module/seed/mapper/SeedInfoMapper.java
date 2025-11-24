package com.linyi.cropseed.trace.module.seed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.module.seed.model.entity.SeedInfo;
import com.linyi.cropseed.trace.module.seed.model.vo.SeedInfoVO;
import com.linyi.cropseed.trace.module.wx.model.vo.WxProductDetailVO;
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

    /**
     * 联表查询微信小程序商品详情
     */
    WxProductDetailVO selectWxProductDetailById(@Param("id") Long id);
}
