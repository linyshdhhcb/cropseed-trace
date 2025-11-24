package com.linyi.cropseed.trace.module.wx.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import com.linyi.cropseed.trace.module.wx.model.dto.CartBatchOperateDTO;
import com.linyi.cropseed.trace.module.wx.model.dto.CartItemDTO;
import com.linyi.cropseed.trace.module.wx.model.dto.CartUpdateDTO;
import com.linyi.cropseed.trace.module.wx.service.CartService;
import com.linyi.cropseed.trace.module.wx.model.vo.CartItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 微信小程序购物车接口
 */
@Tag(name = "微信购物车", description = "微信小程序购物车接口")
@RestController
@RequestMapping("/wx/cart")
@RequiredArgsConstructor
public class WxCartController {

    private final CartService cartService;

    @Operation(summary = "购物车列表")
    @GetMapping
    public Result<List<CartItemVO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<List<CartItemVO>>fail(ResultCode.USER_NOT_LOGIN);
        }
        return Result.success(cartService.listCartItems(userId));
    }

    @Operation(summary = "加入购物车")
    @PostMapping
    public Result<CartItemVO> add(@Valid @RequestBody CartItemDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<CartItemVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        CartItemVO vo = cartService.addItem(userId, dto);
        return Result.success(vo);
    }

    @Operation(summary = "更新购物车")
    @PutMapping("/{id}")
    public Result<CartItemVO> update(@PathVariable Long id, @RequestBody CartUpdateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<CartItemVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        CartItemVO vo = null;
        if (dto.getQuantity() != null) {
            vo = cartService.updateQuantity(userId, id, dto.getQuantity());
        }
        if (dto.getSelected() != null) {
            vo = cartService.updateSelected(userId, id, dto.getSelected());
        }
        return Result.success(vo);
    }

    @Operation(summary = "批量选中/取消")
    @PutMapping("/batch-select")
    public Result<Void> batchSelect(@Valid @RequestBody CartBatchOperateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        cartService.batchUpdateSelected(userId, dto);
        return Result.success();
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        cartService.removeItem(userId, id);
        return Result.success();
    }

    @Operation(summary = "批量删除购物车")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@Valid @RequestBody CartBatchOperateDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        cartService.removeItems(userId, dto.getCartIds());
        return Result.success();
    }

    @Operation(summary = "清空购物车")
    @PostMapping("/clear")
    public Result<Void> clear() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        cartService.clearCart(userId);
        return Result.success();
    }
}
