package com.linyi.cropseed.trace.module.wx.model.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 微信登录DTO
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
public class WxLoginDTO {

    /**
     * 微信授权码
     */
    @NotBlank(message = "授权码不能为空")
    private String code;

    /**
     * 加密数据
     */
    private String encryptedData;

    /**
     * 初始向量
     */
    private String iv;

    /**
     * 用户信息
     */
    private String userInfo;
}
