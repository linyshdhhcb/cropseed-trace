package com.linyi.cropseed.trace.module.wx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.module.wx.model.entity.AfterSales;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 售后 Mapper
 */
@Mapper
public interface AfterSalesMapper extends BaseMapper<AfterSales> {

    /**
     * 查询用户售后列表
     */
    @Select("SELECT * FROM after_sales WHERE user_id = #{userId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<AfterSales> selectByUserId(Long userId);
}
