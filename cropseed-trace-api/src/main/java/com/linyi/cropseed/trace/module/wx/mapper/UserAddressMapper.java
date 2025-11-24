package com.linyi.cropseed.trace.module.wx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.module.wx.model.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户收货地址Mapper
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    /**
     * 根据用户ID查询地址列表
     */
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} AND deleted_flag = 0 ORDER BY is_default DESC, create_time DESC")
    List<UserAddress> selectByUserId(Long userId);

    /**
     * 根据用户ID查询默认地址
     */
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} AND is_default = 1 AND deleted_flag = 0 LIMIT 1")
    UserAddress selectDefaultByUserId(Long userId);

    /**
     * 取消用户所有默认地址
     */
    @Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId} AND deleted_flag = 0")
    int cancelDefaultByUserId(Long userId);
}
