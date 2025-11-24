package com.linyi.cropseed.trace.module.wx.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import com.linyi.cropseed.trace.module.wx.model.dto.AfterSalesApplyDTO;
import com.linyi.cropseed.trace.module.wx.service.AfterSalesService;
import com.linyi.cropseed.trace.module.wx.model.vo.AfterSalesVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 微信小程序售后接口
 */
@Tag(name = "微信售后", description = "微信小程序售后管理")
@RestController
@RequestMapping("/wx/after-sales")
@RequiredArgsConstructor
public class WxAfterSalesController {

    private final AfterSalesService afterSalesService;

    @Operation(summary = "售后列表")
    @GetMapping
    public Result<List<AfterSalesVO>> list(@RequestParam(required = false) Integer status) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<List<AfterSalesVO>>fail(ResultCode.USER_NOT_LOGIN);
        }
        return Result.success(afterSalesService.listByUser(userId, status));
    }

    @Operation(summary = "售后详情")
    @GetMapping("/{id}")
    public Result<AfterSalesVO> detail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<AfterSalesVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        return Result.success(afterSalesService.detail(userId, id));
    }

    @Operation(summary = "提交售后")
    @PostMapping
    public Result<AfterSalesVO> apply(@Valid @RequestBody AfterSalesApplyDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<AfterSalesVO>fail(ResultCode.USER_NOT_LOGIN);
        }
        return Result.success(afterSalesService.apply(userId, dto));
    }

    @Operation(summary = "撤销售后")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        afterSalesService.cancel(userId, id);
        return Result.success();
    }
}
