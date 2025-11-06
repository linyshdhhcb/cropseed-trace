package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.entity.WxUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 微信小程序用户Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface WxUserMapper extends BaseMapper<WxUser> {

    /**
     * 根据openid查询用户
     */
    @Select("SELECT * FROM wx_user WHERE openid = #{openid} AND deleted_flag = 0")
    WxUser selectByOpenid(String openid);

    /**
     * 根据unionid查询用户
     */
    @Select("SELECT * FROM wx_user WHERE unionid = #{unionid} AND deleted_flag = 0")
    WxUser selectByUnionid(String unionid);

    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM wx_user WHERE phone = #{phone} AND deleted_flag = 0")
    WxUser selectByPhone(String phone);
}
