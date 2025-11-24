package com.linyi.cropseed.trace.module.wx.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信小程序登录返回
 */
@Data
public class WxAuthVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "令牌过期时间，单位秒")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private WxUserVO userInfo;
}
