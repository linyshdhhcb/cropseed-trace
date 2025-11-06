package com.linyi.cropseed.trace.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.cropseed.trace.dto.WxLoginDTO;
import com.linyi.cropseed.trace.entity.WxUser;
import com.linyi.cropseed.trace.vo.WxUserVO;

/**
 * 微信用户服务接口
 * 
 * @author LinYi
 * @since 2025-10-25
 */
public interface WxUserService extends IService<WxUser> {

    /**
     * 微信登录
     */
    WxUserVO wxLogin(WxLoginDTO loginDTO);

    /**
     * 根据openid获取用户信息
     */
    WxUserVO getUserByOpenid(String openid);

    /**
     * 更新用户信息
     */
    WxUserVO updateUserInfo(WxUserVO userVO);

    /**
     * 绑定手机号
     */
    WxUserVO bindPhone(Long userId, String phone);

    /**
     * 解绑手机号
     */
    WxUserVO unbindPhone(Long userId);
}
