package com.linyi.cropseed.trace.module.wx.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 用户地址DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class UserAddressDTO {

    /**
     * 地址ID（更新时必填）
     */
    private Long id;

    /**
     * 收货人
     */
    @NotBlank(message = "收货人不能为空")
    private String consignee;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    /**
     * 省份
     */
    @NotBlank(message = "省份不能为空")
    private String province;

    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    private String city;

    /**
     * 区县
     */
    @NotBlank(message = "区县不能为空")
    private String district;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;

    /**
     * 是否默认：0-否，1-是
     */
    @NotNull(message = "是否默认不能为空")
    private Integer isDefault;
}
