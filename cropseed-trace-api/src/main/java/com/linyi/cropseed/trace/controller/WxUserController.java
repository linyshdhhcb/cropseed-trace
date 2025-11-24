package com.linyi.cropseed.trace.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.dto.WxLoginDTO;
import com.linyi.cropseed.trace.module.system.model.entity.SysUser;
import com.linyi.cropseed.trace.entity.WxUser;
import com.linyi.cropseed.trace.service.WxUserService;
import com.linyi.cropseed.trace.vo.WxUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 微信用户控制器
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "微信用户管理", description = "微信小程序用户相关接口")
@RestController
@RequestMapping("/wechat/user")
@RequiredArgsConstructor
public class WxUserController {

    private final WxUserService wxUserService;

    @Operation(summary = "微信登录", description = "微信小程序用户登录")
    @PostMapping("/login")
    public Result<WxUserVO> wxLogin(@Valid @RequestBody WxLoginDTO loginDTO) {
        WxUserVO userVO = wxUserService.wxLogin(loginDTO);
        return Result.success(userVO);
    }

    @Operation(summary = "获取用户信息", description = "根据openid获取用户信息")
    @GetMapping("/info/{openid}")
    public Result<WxUserVO> getUserInfo(@PathVariable String openid) {
        WxUserVO userVO = wxUserService.getUserByOpenid(openid);
        return Result.success(userVO);
    }

    @Operation(summary = "更新用户信息", description = "更新用户基本信息")
    @PutMapping("/info")
    public Result<WxUserVO> updateUserInfo(@Valid @RequestBody WxUserVO userVO) {
        WxUserVO result = wxUserService.updateUserInfo(userVO);
        return Result.success(result);
    }

    @Operation(summary = "绑定手机号", description = "绑定用户手机号")
    @PostMapping("/bind-phone")
    public Result<WxUserVO> bindPhone(@RequestParam String phone) {
        // 从JWT中获取用户ID
        Long userId = getCurrentUserId();
        WxUserVO userVO = wxUserService.bindPhone(userId, phone);
        return Result.success(userVO);
    }

    @Operation(summary = "解绑手机号", description = "解绑用户手机号")
    @PostMapping("/unbind-phone")
    public Result<WxUserVO> unbindPhone() {
        // 从JWT中获取用户ID
        Long userId = getCurrentUserId();
        WxUserVO userVO = wxUserService.unbindPhone(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "分页查询微信用户列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('wechat:user:list')")
    public Result<PageResult<WxUserVO>> pageWxUsers(PageQuery pageQuery,
            @Parameter(description = "昵称") @RequestParam(required = false) String nickname,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<WxUser> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        LambdaQueryWrapper<WxUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(nickname), WxUser::getNickname, nickname)
                .like(StringUtils.hasText(phone), WxUser::getPhone, phone)
                .eq(status != null, WxUser::getStatus, status)
                .orderByDesc(WxUser::getCreateTime);

        Page<WxUser> result = wxUserService.page(page, queryWrapper);

        List<WxUserVO> voList = result.getRecords().stream()
                .map(user -> {
                    WxUserVO vo = new WxUserVO();
                    vo.setId(user.getId());
                    vo.setOpenid(user.getOpenid());
                    vo.setUnionid(user.getUnionid());
                    vo.setNickname(user.getNickname());
                    vo.setAvatarUrl(user.getAvatarUrl());
                    vo.setGender(user.getGender());
                    vo.setCountry(user.getCountry());
                    vo.setProvince(user.getProvince());
                    vo.setCity(user.getCity());
                    vo.setPhone(user.getPhone());
                    vo.setStatus(user.getStatus());
                    vo.setLastLoginTime(user.getLastLoginTime());
                    vo.setCreateTime(user.getCreateTime());
                    return vo;
                })
                .collect(Collectors.toList());

        return Result.success(PageResult.of(result, voList));
    }

    @Operation(summary = "根据ID获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('wechat:user:query')")
    public Result<WxUserVO> getWxUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        WxUser user = wxUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        WxUserVO vo = new WxUserVO();
        vo.setId(user.getId());
        vo.setOpenid(user.getOpenid());
        vo.setUnionid(user.getUnionid());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setGender(user.getGender());
        vo.setCountry(user.getCountry());
        vo.setProvince(user.getProvince());
        vo.setCity(user.getCity());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());

        return Result.success(vo);
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/status/{id}")
    @PreAuthorize("hasAuthority('wechat:user:edit')")
    public Result<Void> updateWxUserStatus(@Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        WxUser user = wxUserService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setStatus(status);
        wxUserService.updateById(user);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('wechat:user:remove')")
    public Result<Void> deleteWxUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        wxUserService.removeById(id);
        return Result.success("删除成功");
    }

    private Long getCurrentUserId() {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SysUser) {
            SysUser user = (SysUser) authentication.getPrincipal();
            return user.getId();
        }
        return 1L; // 默认用户ID，实际项目中应该抛出异常
    }
}
