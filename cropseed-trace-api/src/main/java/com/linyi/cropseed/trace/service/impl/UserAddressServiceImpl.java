package com.linyi.cropseed.trace.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.IdGenerator;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import com.linyi.cropseed.trace.dto.UserAddressDTO;
import com.linyi.cropseed.trace.entity.UserAddress;
import com.linyi.cropseed.trace.mapper.UserAddressMapper;
import com.linyi.cropseed.trace.service.UserAddressService;
import com.linyi.cropseed.trace.vo.UserAddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户地址服务实现
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    private final UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddressVO> getUserAddressList(Long userId) {
        List<UserAddress> addresses = userAddressMapper.selectByUserId(userId);
        return addresses.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAddressVO addAddress(Long userId, UserAddressDTO addressDTO) {
        // 如果设置为默认地址，先取消其他默认地址
        if (addressDTO.getIsDefault() == 1) {
            userAddressMapper.cancelDefaultByUserId(userId);
        }

        UserAddress address = new UserAddress();
        address.setId(IdGenerator.generateId());
        address.setUserId(userId);
        address.setConsignee(addressDTO.getConsignee());
        address.setPhone(addressDTO.getPhone());
        address.setProvince(addressDTO.getProvince());
        address.setCity(addressDTO.getCity());
        address.setDistrict(addressDTO.getDistrict());
        address.setDetailAddress(addressDTO.getDetailAddress());
        address.setIsDefault(addressDTO.getIsDefault());

        save(address);
        return convertToVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAddressVO updateAddress(Long userId, UserAddressDTO addressDTO) {
        UserAddress address = getById(addressDTO.getId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        // 如果设置为默认地址，先取消其他默认地址
        if (addressDTO.getIsDefault() == 1) {
            userAddressMapper.cancelDefaultByUserId(userId);
        }

        address.setConsignee(addressDTO.getConsignee());
        address.setPhone(addressDTO.getPhone());
        address.setProvince(addressDTO.getProvince());
        address.setCity(addressDTO.getCity());
        address.setDistrict(addressDTO.getDistrict());
        address.setDetailAddress(addressDTO.getDetailAddress());
        address.setIsDefault(addressDTO.getIsDefault());

        updateById(address);
        return convertToVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long userId, Long addressId) {
        UserAddress address = getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        return removeById(addressId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long userId, Long addressId) {
        UserAddress address = getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        // 先取消其他默认地址
        userAddressMapper.cancelDefaultByUserId(userId);

        // 设置当前地址为默认
        address.setIsDefault(1);
        return updateById(address);
    }

    @Override
    public UserAddressVO getDefaultAddress(Long userId) {
        UserAddress address = userAddressMapper.selectDefaultByUserId(userId);
        return address != null ? convertToVO(address) : null;
    }

    @Override
    public UserAddressVO getAddressById(Long userId, Long addressId) {
        UserAddress address = getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }
        return convertToVO(address);
    }

    /**
     * 转换为VO
     */
    private UserAddressVO convertToVO(UserAddress address) {
        UserAddressVO vo = BeanUtil.copyProperties(address, UserAddressVO.class);
        // 拼接完整地址
        vo.setFullAddress(
                address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        return vo;
    }
}
