package com.linyi.cropseed.trace.module.wx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.cropseed.trace.module.wx.model.dto.UserAddressDTO;
import com.linyi.cropseed.trace.module.wx.model.entity.UserAddress;
import com.linyi.cropseed.trace.module.wx.model.vo.UserAddressVO;

import java.util.List;

/**
 * 用户地址服务接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface UserAddressService extends IService<UserAddress> {

    /**
     * 获取用户地址列表
     */
    List<UserAddressVO> getUserAddressList(Long userId);

    /**
     * 添加地址
     */
    UserAddressVO addAddress(Long userId, UserAddressDTO addressDTO);

    /**
     * 更新地址
     */
    UserAddressVO updateAddress(Long userId, UserAddressDTO addressDTO);

    /**
     * 删除地址
     */
    boolean deleteAddress(Long userId, Long addressId);

    /**
     * 设置默认地址
     */
    boolean setDefaultAddress(Long userId, Long addressId);

    /**
     * 获取默认地址
     */
    UserAddressVO getDefaultAddress(Long userId);

    /**
     * 根据ID获取地址
     */
    UserAddressVO getAddressById(Long userId, Long addressId);
}
