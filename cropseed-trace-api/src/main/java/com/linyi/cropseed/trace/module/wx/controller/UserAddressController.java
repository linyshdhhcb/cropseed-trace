package com.linyi.cropseed.trace.module.wx.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.module.wx.model.dto.UserAddressDTO;
import com.linyi.cropseed.trace.module.system.model.entity.SysUser;
import com.linyi.cropseed.trace.module.wx.service.UserAddressService;
import com.linyi.cropseed.trace.module.wx.model.vo.UserAddressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 用户地址控制器
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "用户地址管理", description = "用户收货地址相关接口")
@RestController
@RequestMapping("/api/wechat/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @Operation(summary = "获取地址列表", description = "获取用户收货地址列表")
    @GetMapping("/list")
    public Result<List<UserAddressVO>> getAddressList() {
        // 从JWT中获取用户ID
        Long userId = getCurrentUserId();
        List<UserAddressVO> addresses = userAddressService.getUserAddressList(userId);
        return Result.success(addresses);
    }

    @Operation(summary = "添加地址", description = "添加新的收货地址")
    @PostMapping("/add")
    public Result<UserAddressVO> addAddress(@Valid @RequestBody UserAddressDTO addressDTO) {
        // 从JWT中获取用户ID
        Long userId = getCurrentUserId();
        UserAddressVO address = userAddressService.addAddress(userId, addressDTO);
        return Result.success(address);
    }

    @Operation(summary = "更新地址", description = "更新收货地址信息")
    @PutMapping("/update")
    public Result<UserAddressVO> updateAddress(@Valid @RequestBody UserAddressDTO addressDTO) {
        // 从JWT中获取用户ID
        Long userId = getCurrentUserId();
        UserAddressVO address = userAddressService.updateAddress(userId, addressDTO);
        return Result.success(address);
    }

    @Operation(summary = "删除地址", description = "删除收货地址")
    @DeleteMapping("/delete/{addressId}")
    public Result<Boolean> deleteAddress(@PathVariable Long addressId) {
        // 从JWT中获取用户ID
        Long userId = getCurrentUserId();
        boolean result = userAddressService.deleteAddress(userId, addressId);
        return Result.success(result);
    }

    @Operation(summary = "设置默认地址", description = "设置默认收货地址")
    @PostMapping("/set-default/{addressId}")
    public Result<Boolean> setDefaultAddress(@PathVariable Long addressId) {
        // 从JWT中获取用户ID
        Long userId = getCurrentUserId();
        boolean result = userAddressService.setDefaultAddress(userId, addressId);
        return Result.success(result);
    }

    @Operation(summary = "获取默认地址", description = "获取用户默认收货地址")
    @GetMapping("/default")
    public Result<UserAddressVO> getDefaultAddress() {
        // 从JWT中获取用户ID
        Long userId = getCurrentUserId();
        UserAddressVO address = userAddressService.getDefaultAddress(userId);
        return Result.success(address);
    }

    @Operation(summary = "获取地址详情", description = "根据ID获取地址详情")
    @GetMapping("/{addressId}")
    public Result<UserAddressVO> getAddressById(@PathVariable Long addressId) {
        // 从JWT中获取用户ID
        Long userId = getCurrentUserId();
        UserAddressVO address = userAddressService.getAddressById(userId, addressId);
        return Result.success(address);
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
