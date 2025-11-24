package com.linyi.cropseed.trace.module.wx.controller;

import cn.hutool.core.bean.BeanUtil;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import com.linyi.cropseed.trace.module.wx.model.entity.WxUser;
import com.linyi.cropseed.trace.module.wx.service.WxUserService;
import com.linyi.cropseed.trace.module.wx.model.vo.WxUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信小程序用户资料控制器
 */
@Tag(name = "微信用户", description = "微信小程序用户资料接口")
@RestController
@RequestMapping("/wx/user")
@RequiredArgsConstructor
public class WxUserProfileController {

    private final WxUserService wxUserService;

    @Operation(summary = "获取用户资料")
    @GetMapping("/profile")
    public Result<WxUserVO> profile() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<WxUserVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        WxUser wxUser = wxUserService.getById(userId);
        if (wxUser == null) {
            return Result.<WxUserVO>fail(ResultCode.WX_USER_NOT_EXIST);
        }
        WxUserVO userVO = BeanUtil.copyProperties(wxUser, WxUserVO.class);
        return Result.success(userVO);
    }

    @Operation(summary = "更新用户资料")
    @PutMapping("/profile")
    public Result<WxUserVO> updateProfile(@Valid @RequestBody WxUserVO userVO) {
        if (SecurityUtils.getCurrentUserId() == null) {
            return Result.<WxUserVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        WxUserVO result = wxUserService.updateUserInfo(userVO);
        return Result.success(result);
    }
}
