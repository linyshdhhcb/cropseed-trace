package com.linyi.cropseed.trace.controller;

import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import com.linyi.cropseed.trace.dto.UserBehaviorReportDTO;
import com.linyi.cropseed.trace.service.RecommendationService;
import com.linyi.cropseed.trace.vo.RecommendationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 微信小程序推荐接口
 */
@Tag(name = "微信推荐", description = "微信小程序推荐与行为上报")
@RestController
@RequestMapping("/wx/recommend")
@RequiredArgsConstructor
public class WxRecommendController {

    private final RecommendationService recommendationService;

    @Operation(summary = "推荐商品列表")
    @GetMapping("/list")
    public Result<List<RecommendationVO>> recommend(@RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<RecommendationVO> list;
        if (userId == null) {
            // 未登录用户返回热门推荐
            list = recommendationService.popularRecommend(limit);
        } else {
            // 已登录用户返回个性化推荐
            list = recommendationService.hybridRecommend(userId, limit);
        }
        return Result.success(list);
    }

    @Operation(summary = "用户行为上报")
    @PostMapping("/behavior")
    public Result<Void> reportBehavior(@Valid @RequestBody UserBehaviorReportDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.<Void>fail(ResultCode.USER_NOT_LOGIN);
        }
        recommendationService.recordUserBehavior(userId, dto.getBehaviorType(), 1, dto.getSeedId(), dto.getSource(),
                dto.getDuration() == null ? 1.0 : dto.getDuration().doubleValue());
        return Result.success();
    }
}
