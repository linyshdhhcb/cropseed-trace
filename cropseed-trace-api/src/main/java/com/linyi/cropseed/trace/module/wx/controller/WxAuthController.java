package com.linyi.cropseed.trace.module.wx.controller;

import com.linyi.cropseed.trace.common.constant.CommonConstant;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.module.wx.model.dto.WxLoginDTO;
import com.linyi.cropseed.trace.security.JwtTokenProvider;
import com.linyi.cropseed.trace.module.wx.service.WxUserService;
import com.linyi.cropseed.trace.module.wx.model.vo.WxAuthVO;
import com.linyi.cropseed.trace.module.wx.model.vo.WxUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信小程序认证控制器
 */
@Tag(name = "微信认证", description = "微信小程序登录、登出接口")
@RestController
@RequestMapping("/wx/auth")
@RequiredArgsConstructor
public class WxAuthController {

    private static final String WX_USERNAME_PREFIX = "WX:";

    private final WxUserService wxUserService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Operation(summary = "微信授权登录")
    @PostMapping("/login")
    public Result<WxAuthVO> login(@Valid @RequestBody WxLoginDTO loginDTO) {
        WxUserVO userVO = wxUserService.wxLogin(loginDTO);
        if (userVO == null) {
            return Result.fail(ResultCode.WX_LOGIN_FAILED);
        }

        String token = jwtTokenProvider.generateToken(userVO.getId(), WX_USERNAME_PREFIX + userVO.getOpenid());

        WxAuthVO authVO = new WxAuthVO();
        authVO.setToken(token);
        authVO.setExpiresIn(jwtExpiration);
        authVO.setUserInfo(userVO);
        return Result.success(authVO);
    }

    @Operation(summary = "微信登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader(CommonConstant.TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(CommonConstant.TOKEN_PREFIX)) {
            String token = bearerToken.substring(CommonConstant.TOKEN_PREFIX.length());
            jwtTokenProvider.addTokenToBlacklist(token);
        }
        return Result.success();
    }
}
