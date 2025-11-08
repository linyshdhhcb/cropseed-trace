package com.linyi.cropseed.trace.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.Result;
import com.linyi.cropseed.trace.entity.Recommendation;
import com.linyi.cropseed.trace.entity.UserBehavior;
import com.linyi.cropseed.trace.entity.UserProfile;
import com.linyi.cropseed.trace.mapper.RecommendationMapper;
import com.linyi.cropseed.trace.mapper.UserBehaviorMapper;
import com.linyi.cropseed.trace.mapper.UserProfileMapper;
import com.linyi.cropseed.trace.service.RecommendationService;
import com.linyi.cropseed.trace.vo.RecommendationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐系统控制器
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Tag(name = "推荐系统管理", description = "推荐系统相关接口")
@RestController
@RequestMapping("/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final RecommendationMapper recommendationMapper;
    private final UserBehaviorMapper userBehaviorMapper;
    private final UserProfileMapper userProfileMapper;

    @Operation(summary = "协同过滤推荐", description = "基于用户相似性的协同过滤推荐")
    @GetMapping("/collaborative/{userId}")
    public Result<List<RecommendationVO>> collaborativeFilteringRecommend(@PathVariable Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<RecommendationVO> recommendations = recommendationService.collaborativeFilteringRecommend(userId, limit);
        return Result.success(recommendations);
    }

    @Operation(summary = "内容推荐", description = "基于商品特征的内容推荐")
    @GetMapping("/content/{userId}")
    public Result<List<RecommendationVO>> contentBasedRecommend(@PathVariable Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<RecommendationVO> recommendations = recommendationService.contentBasedRecommend(userId, limit);
        return Result.success(recommendations);
    }

    @Operation(summary = "热门推荐", description = "基于市场热度的热门商品推荐")
    @GetMapping("/popular")
    public Result<List<RecommendationVO>> popularRecommend(@RequestParam(defaultValue = "10") Integer limit) {
        List<RecommendationVO> recommendations = recommendationService.popularRecommend(limit);
        return Result.success(recommendations);
    }

    @Operation(summary = "个性化推荐", description = "基于用户画像的个性化推荐")
    @GetMapping("/personalized/{userId}")
    public Result<List<RecommendationVO>> personalizedRecommend(@PathVariable Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<RecommendationVO> recommendations = recommendationService.personalizedRecommend(userId, limit);
        return Result.success(recommendations);
    }

    @Operation(summary = "混合推荐", description = "综合多种算法的混合推荐")
    @GetMapping("/hybrid/{userId}")
    public Result<List<RecommendationVO>> hybridRecommend(@PathVariable Long userId,
            @RequestParam(defaultValue = "20") Integer limit) {
        List<RecommendationVO> recommendations = recommendationService.hybridRecommend(userId, limit);
        return Result.success(recommendations);
    }

    @Operation(summary = "记录用户行为", description = "记录用户浏览、搜索、购买等行为")
    @PostMapping("/behavior")
    public Result<Void> recordUserBehavior(@RequestParam Long userId,
            @RequestParam Integer behaviorType,
            @RequestParam Integer targetType,
            @RequestParam Long targetId,
            @RequestParam(required = false) String behaviorContent,
            @RequestParam(defaultValue = "1.0") Double behaviorWeight) {
        recommendationService.recordUserBehavior(userId, behaviorType, targetType, targetId, behaviorContent,
                behaviorWeight);
        return Result.success();
    }

    @Operation(summary = "更新用户画像", description = "基于用户行为更新用户画像")
    @PostMapping("/profile/update/{userId}")
    public Result<Void> updateUserProfile(@PathVariable Long userId) {
        recommendationService.updateUserProfile(userId);
        return Result.success();
    }

    @Operation(summary = "计算商品特征", description = "计算商品的特征向量")
    @PostMapping("/features/calculate/{seedId}")
    public Result<Void> calculateSeedFeatures(@PathVariable Long seedId) {
        recommendationService.calculateSeedFeatures(seedId);
        return Result.success();
    }

    @Operation(summary = "生成推荐结果", description = "为指定用户生成推荐结果")
    @PostMapping("/generate/{userId}")
    public Result<Void> generateRecommendations(@PathVariable Long userId) {
        recommendationService.generateRecommendations(userId);
        return Result.success();
    }

    @Operation(summary = "记录推荐点击", description = "记录用户点击推荐商品")
    @PostMapping("/click/{recommendationId}")
    public Result<Void> recordRecommendationClick(@PathVariable Long recommendationId) {
        recommendationService.recordRecommendationClick(recommendationId);
        return Result.success();
    }

    @Operation(summary = "记录推荐购买", description = "记录用户购买推荐商品")
    @PostMapping("/purchase/{recommendationId}")
    public Result<Void> recordRecommendationPurchase(@PathVariable Long recommendationId) {
        recommendationService.recordRecommendationPurchase(recommendationId);
        return Result.success();
    }

    @Operation(summary = "分页查询推荐结果", description = "分页查询推荐结果列表")
    @GetMapping("/page")
    public Result<PageResult<Recommendation>> getRecommendationPage(PageQuery pageQuery,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer recommendationType) {
        Page<Recommendation> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<Recommendation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recommendation::getDeletedFlag, 0);
        if (userId != null) {
            wrapper.eq(Recommendation::getUserId, userId);
        }
        if (recommendationType != null) {
            wrapper.eq(Recommendation::getRecommendationType, recommendationType);
        }
        wrapper.orderByDesc(Recommendation::getRecommendationScore);
        Page<Recommendation> result = recommendationMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(result));
    }

    @Operation(summary = "分页查询用户行为", description = "分页查询用户行为列表")
    @GetMapping("/behavior/page")
    public Result<PageResult<UserBehavior>> getUserBehaviorPage(PageQuery pageQuery,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer behaviorType) {
        Page<UserBehavior> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getDeletedFlag, 0);
        if (userId != null) {
            wrapper.eq(UserBehavior::getUserId, userId);
        }
        if (behaviorType != null) {
            wrapper.eq(UserBehavior::getBehaviorType, behaviorType);
        }
        wrapper.orderByDesc(UserBehavior::getBehaviorTime);
        Page<UserBehavior> result = userBehaviorMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(result));
    }

    @Operation(summary = "分页查询用户画像", description = "分页查询用户画像列表")
    @GetMapping("/profile/page")
    public Result<PageResult<UserProfile>> getUserProfilePage(PageQuery pageQuery,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer userType) {
        Page<UserProfile> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfile::getDeletedFlag, 0);
        if (userId != null) {
            wrapper.eq(UserProfile::getUserId, userId);
        }
        if (userType != null) {
            wrapper.eq(UserProfile::getUserType, userType);
        }
        wrapper.orderByDesc(UserProfile::getCreateTime);
        Page<UserProfile> result = userProfileMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(result));
    }

    @Operation(summary = "获取用户画像详情", description = "根据用户ID获取用户画像详情")
    @GetMapping("/profile/{userId}")
    public Result<UserProfile> getUserProfile(@PathVariable Long userId) {
        UserProfile profile = userProfileMapper.selectByUserId(userId);
        return Result.success(profile);
    }
}
