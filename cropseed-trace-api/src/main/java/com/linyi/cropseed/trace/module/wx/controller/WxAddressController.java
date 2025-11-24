package com.linyi.cropseed.trace.module.wx.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import com.linyi.cropseed.trace.module.wx.model.dto.UserAddressDTO;
import com.linyi.cropseed.trace.module.wx.service.UserAddressService;
import com.linyi.cropseed.trace.module.wx.model.vo.UserAddressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 微信小程序地址管理
 */
@Tag(name = "微信地址", description = "微信小程序收货地址管理")
@RestController
@RequestMapping("/wx/address")
@RequiredArgsConstructor
public class WxAddressController {

    private final UserAddressService userAddressService;

    @Operation(summary = "地址列表")
    @GetMapping
    public Result<List<UserAddressVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<List<UserAddressVO>>fail(ResultCode.USER_NOT_LOGIN);
        }
        List<UserAddressVO> list = userAddressService.getUserAddressList(userId);
        return Result.success(list);
    }

    @Operation(summary = "新增地址")
    @PostMapping
    public Result<UserAddressVO> add(@Valid @RequestBody UserAddressDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<UserAddressVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        UserAddressVO vo = userAddressService.addAddress(userId, dto);
        return Result.success(vo);
    }

    @Operation(summary = "更新地址")
    @PutMapping("/{id}")
    public Result<UserAddressVO> update(@PathVariable Long id, @Valid @RequestBody UserAddressDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<UserAddressVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        dto.setId(id);
        UserAddressVO vo = userAddressService.updateAddress(userId, dto);
        return Result.success(vo);
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        userAddressService.deleteAddress(userId, id);
        return Result.success();
    }

    @Operation(summary = "设置默认地址")
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        userAddressService.setDefaultAddress(userId, id);
        return Result.success();
    }

    @Operation(summary = "默认地址")
    @GetMapping("/default")
    public Result<UserAddressVO> defaultAddress() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<UserAddressVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        UserAddressVO vo = userAddressService.getDefaultAddress(userId);
        return Result.success(vo);
    }

    @Operation(summary = "地址详情")
    @GetMapping("/{id}")
    public Result<UserAddressVO> detail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<UserAddressVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        UserAddressVO vo = userAddressService.getAddressById(userId, id);
        return Result.success(vo);
    }
}
