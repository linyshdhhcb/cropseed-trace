package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 购物车Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 根据用户ID查询购物车
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<Cart> selectByUserId(Long userId);

    /**
     * 根据用户ID和种子ID查询购物车商品
     */
    @Select("SELECT * FROM cart WHERE user_id = #{userId} AND seed_id = #{seedId} AND deleted_flag = 0")
    Cart selectByUserIdAndSeedId(Long userId, Long seedId);
}
