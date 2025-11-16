package com.linyi.cropseed.trace.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.util.IdGenerator;
import com.linyi.cropseed.trace.entity.*;
import com.linyi.cropseed.trace.mapper.*;
import com.linyi.cropseed.trace.service.RecommendationService;
import com.linyi.cropseed.trace.vo.RecommendationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 推荐系统服务实现
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl extends ServiceImpl<RecommendationMapper, Recommendation>
        implements RecommendationService {

    private final UserProfileMapper userProfileMapper;
    private final UserBehaviorMapper userBehaviorMapper;
    private final SeedFeaturesMapper seedFeaturesMapper;
    private final SeedInfoMapper seedInfoMapper;
    private final RecommendationMapper recommendationMapper;

    @Override
    public List<RecommendationVO> collaborativeFilteringRecommend(Long userId, Integer limit) {
        try {
            // 1. 获取用户画像
            UserProfile userProfile = userProfileMapper.selectByUserId(userId);
            if (userProfile == null) {
                return Collections.emptyList();
            }

            // 2. 获取用户历史行为用于计算相似度
            List<UserBehavior> userBehaviors = userBehaviorMapper.selectByUserId(userId, 100);
            if (CollUtil.isEmpty(userBehaviors)) {
                // 如果用户没有行为记录，返回热门推荐
                return popularRecommend(limit);
            }

            // 3. 找到相似用户（基于多维度相似度计算）
            List<UserProfile> candidateProfiles = userProfileMapper.selectSimilarProfiles(
                    userProfile.getUserType(), userProfile.getRegion(), 50);

            if (CollUtil.isEmpty(candidateProfiles)) {
                return Collections.emptyList();
            }

            // 4. 计算用户相似度并筛选最相似的用户
            Map<Long, Double> userSimilarities = calculateUserSimilarities(userId, userBehaviors, candidateProfiles);
            List<Long> similarUserIds = userSimilarities.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0.1) // 相似度阈值
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(10)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (similarUserIds.isEmpty()) {
                return Collections.emptyList();
            }

            // 5. 获取相似用户的行为记录
            List<UserBehavior> similarBehaviors = new ArrayList<>();
            for (Long similarUserId : similarUserIds) {
                List<UserBehavior> behaviors = userBehaviorMapper.selectByUserId(similarUserId, 50);
                similarBehaviors.addAll(behaviors);
            }

            // 6. 统计商品评分（修复空指针问题）
            Map<Long, Double> itemScores = new HashMap<>();
            Set<Long> userPurchasedItems = userBehaviors.stream()
                    .filter(b -> b.getBehaviorType() == 5) // 用户已购买的商品
                    .map(UserBehavior::getTargetId)
                    .collect(Collectors.toSet());

            for (UserBehavior behavior : similarBehaviors) {
                if (behavior.getBehaviorType() == 5 || behavior.getBehaviorType() == 6) { // 购买或评价
                    Long targetId = behavior.getTargetId();
                    
                    // 跳过用户已购买的商品
                    if (userPurchasedItems.contains(targetId)) {
                        continue;
                    }
                    
                    // 安全的评分计算，避免空指针
                    Double behaviorWeight = behavior.getBehaviorWeight() != null ? behavior.getBehaviorWeight() : 1.0;
                    Integer rating = behavior.getRating() != null ? behavior.getRating() : 3;
                    Double userSimilarity = userSimilarities.getOrDefault(behavior.getUserId(), 0.5);
                    
                    Double score = behaviorWeight * (rating / 5.0) * userSimilarity;
                    itemScores.merge(targetId, score, Double::sum);
                }
            }

            // 5. 排序并返回推荐结果
            return itemScores.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> {
                        RecommendationVO vo = new RecommendationVO();
                        vo.setTargetId(entry.getKey());
                        vo.setRecommendationScore(BigDecimal.valueOf(entry.getValue()));
                        vo.setRecommendationType(1);
                        vo.setRecommendationTypeName("协同过滤");
                        vo.setRecommendationReason("基于相似用户的购买行为推荐");
                        // 填充商品信息
                        fillProductInfo(vo, entry.getKey());
                        return vo;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("协同过滤推荐失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecommendationVO> contentBasedRecommend(Long userId, Integer limit) {
        try {
            // 1. 获取用户历史行为
            List<UserBehavior> userBehaviors = userBehaviorMapper.selectByUserId(userId, 100);
            if (CollUtil.isEmpty(userBehaviors)) {
                return Collections.emptyList();
            }

            // 2. 分析用户偏好
            Map<Long, Double> userPreferences = analyzeUserPreferences(userBehaviors);

            // 3. 基于内容特征推荐
            List<RecommendationVO> recommendations = new ArrayList<>();
            for (Map.Entry<Long, Double> entry : userPreferences.entrySet()) {
                Long seedId = entry.getKey();
                Double preference = entry.getValue();

                // 获取商品特征
                SeedFeatures features = seedFeaturesMapper.selectBySeedId(seedId);
                if (features != null) {
                    // 计算相似度
                    List<SeedFeatures> similarFeatures = seedFeaturesMapper.selectSimilarFeatures(
                            seedId, features.getPriceFeature().doubleValue(),
                            features.getQualityFeature().doubleValue(),
                            features.getBrandFeature().doubleValue(), limit);

                    for (SeedFeatures similar : similarFeatures) {
                        RecommendationVO vo = new RecommendationVO();
                        vo.setTargetId(similar.getSeedId());
                        vo.setRecommendationScore(
                                BigDecimal.valueOf(preference * similar.getRecommendationWeight().doubleValue()));
                        vo.setRecommendationType(2);
                        vo.setRecommendationTypeName("内容推荐");
                        vo.setRecommendationReason("基于商品特征相似性推荐");
                        // 填充商品信息
                        fillProductInfo(vo, similar.getSeedId());
                        recommendations.add(vo);
                    }
                }
            }

            // 4. 去重并排序
            return recommendations.stream()
                    .collect(Collectors.toMap(
                            RecommendationVO::getTargetId,
                            vo -> vo,
                            (existing,
                                    replacement) -> existing.getRecommendationScore()
                                            .compareTo(replacement.getRecommendationScore()) > 0 ? existing
                                                    : replacement))
                    .values()
                    .stream()
                    .sorted((a, b) -> b.getRecommendationScore().compareTo(a.getRecommendationScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("内容推荐失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecommendationVO> popularRecommend(Integer limit) {
        try {
            // 1. 获取热门商品特征
            List<SeedFeatures> popularFeatures = seedFeaturesMapper.selectPopularFeatures(0.5, limit * 2);

            // 2. 转换为推荐结果并填充商品信息
            return popularFeatures.stream()
                    .map(features -> {
                        RecommendationVO vo = new RecommendationVO();
                        vo.setTargetId(features.getSeedId());
                        vo.setRecommendationScore(features.getMarketHeat());
                        vo.setRecommendationType(3);
                        vo.setRecommendationTypeName("热门推荐");
                        vo.setRecommendationReason("基于市场热度商品推荐");
                        vo.setRecommendationWeight(BigDecimal.valueOf(0.2));
                        // 填充商品信息
                        fillProductInfo(vo, features.getSeedId());
                        return vo;
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("热门推荐失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecommendationVO> personalizedRecommend(Long userId, Integer limit) {
        try {
            // 1. 获取用户画像
            UserProfile userProfile = userProfileMapper.selectByUserId(userId);
            if (userProfile == null) {
                return Collections.emptyList();
            }

            // 2. 基于用户画像推荐
            List<RecommendationVO> recommendations = new ArrayList<>();

            // 价格敏感度推荐
            if (userProfile.getPriceSensitivity().doubleValue() > 0.7) {
                List<SeedFeatures> priceFeatures = seedFeaturesMapper.selectPriceRangeFeatures(0.0, 0.3, limit / 2);
                recommendations.addAll(convertToRecommendations(priceFeatures, 4, "基于价格敏感度的个性化推荐"));
            }

            // 质量要求推荐
            if (userProfile.getQualityRequirement().doubleValue() > 0.7) {
                List<SeedFeatures> qualityFeatures = seedFeaturesMapper.selectHighQualityFeatures(0.7, limit / 2);
                recommendations.addAll(convertToRecommendations(qualityFeatures, 4, "基于质量要求的个性化推荐"));
            }

            // 3. 去重并排序
            return recommendations.stream()
                    .collect(Collectors.toMap(
                            RecommendationVO::getTargetId,
                            vo -> vo,
                            (existing,
                                    replacement) -> existing.getRecommendationScore()
                                            .compareTo(replacement.getRecommendationScore()) > 0 ? existing
                                                    : replacement))
                    .values()
                    .stream()
                    .sorted((a, b) -> b.getRecommendationScore().compareTo(a.getRecommendationScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("个性化推荐失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecommendationVO> hybridRecommend(Long userId, Integer limit) {
        try {
            List<RecommendationVO> allRecommendations = new ArrayList<>();

            // 1. 协同过滤推荐 (30%)
            List<RecommendationVO> cfRecommendations = collaborativeFilteringRecommend(userId, limit / 4);
            cfRecommendations.forEach(rec -> rec.setRecommendationWeight(BigDecimal.valueOf(0.3)));
            allRecommendations.addAll(cfRecommendations);

            // 2. 内容推荐 (25%)
            List<RecommendationVO> cbRecommendations = contentBasedRecommend(userId, limit / 4);
            cbRecommendations.forEach(rec -> rec.setRecommendationWeight(BigDecimal.valueOf(0.25)));
            allRecommendations.addAll(cbRecommendations);

            // 3. 个性化推荐 (25%)
            List<RecommendationVO> personalizedRecommendations = personalizedRecommend(userId, limit / 4);
            personalizedRecommendations.forEach(rec -> rec.setRecommendationWeight(BigDecimal.valueOf(0.25)));
            allRecommendations.addAll(personalizedRecommendations);

            // 4. 热门推荐 (20%)
            List<RecommendationVO> popularRecommendations = popularRecommend(limit / 4);
            popularRecommendations.forEach(rec -> rec.setRecommendationWeight(BigDecimal.valueOf(0.2)));
            allRecommendations.addAll(popularRecommendations);

            // 5. 混合排序
            return allRecommendations.stream()
                    .collect(Collectors.toMap(
                            RecommendationVO::getTargetId,
                            vo -> vo,
                            (existing, replacement) -> {
                                BigDecimal existingScore = (existing.getRecommendationScore() == null ? BigDecimal.ZERO
                                        : existing.getRecommendationScore())
                                        .multiply(existing.getRecommendationWeight() == null ? BigDecimal.ONE
                                                : existing.getRecommendationWeight());
                                BigDecimal replacementScore = (replacement.getRecommendationScore() == null
                                        ? BigDecimal.ZERO
                                        : replacement.getRecommendationScore())
                                        .multiply(replacement.getRecommendationWeight() == null ? BigDecimal.ONE
                                                : replacement.getRecommendationWeight());
                                return existingScore.compareTo(replacementScore) > 0 ? existing : replacement;
                            }))
                    .values()
                    .stream()
                    .sorted((a, b) -> {
                        BigDecimal scoreA = (a.getRecommendationScore() == null ? BigDecimal.ZERO
                                : a.getRecommendationScore())
                                .multiply(a.getRecommendationWeight() == null ? BigDecimal.ONE
                                        : a.getRecommendationWeight());
                        BigDecimal scoreB = (b.getRecommendationScore() == null ? BigDecimal.ZERO
                                : b.getRecommendationScore())
                                .multiply(b.getRecommendationWeight() == null ? BigDecimal.ONE
                                        : b.getRecommendationWeight());
                        return scoreB.compareTo(scoreA);
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("混合推荐失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordUserBehavior(Long userId, Integer behaviorType, Integer targetType, Long targetId,
            String behaviorContent, Double behaviorWeight) {
        try {
            UserBehavior behavior = new UserBehavior();
            behavior.setId(IdGenerator.generateId());
            behavior.setUserId(userId);
            behavior.setBehaviorType(behaviorType);
            behavior.setTargetType(targetType);
            behavior.setTargetId(targetId);
            behavior.setBehaviorContent(behaviorContent);
            behavior.setBehaviorWeight(behaviorWeight);
            behavior.setBehaviorTime(LocalDateTime.now());
            behavior.setSessionId(IdUtil.fastSimpleUUID());

            userBehaviorMapper.insert(behavior);

            // 异步更新用户画像（避免同步性能问题）
            CompletableFuture.runAsync(() -> {
                try {
                    updateUserProfile(userId);
                } catch (Exception e) {
                    log.warn("异步更新用户画像失败，userId: {}", userId, e);
                }
            });

        } catch (Exception e) {
            log.error("记录用户行为失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserProfile(Long userId) {
        try {
            // 1. 获取用户行为统计
            List<UserBehavior> behaviors = userBehaviorMapper.selectByUserId(userId, 1000);
            if (CollUtil.isEmpty(behaviors)) {
                return;
            }

            // 2. 计算用户画像指标
            UserProfile profile = userProfileMapper.selectByUserId(userId);
            if (profile == null) {
                profile = new UserProfile();
                profile.setId(IdGenerator.generateId());
                profile.setUserId(userId);
            }

            // 计算活跃度
            long recentBehaviors = behaviors.stream()
                    .filter(b -> b.getBehaviorTime().isAfter(LocalDateTime.now().minusDays(30)))
                    .count();
            profile.setActivityLevel(BigDecimal.valueOf(Math.min(recentBehaviors / 30.0, 1.0)));

            // 计算购买频率
            long purchaseCount = userBehaviorMapper.countUserBehaviors(userId, 5);
            profile.setPurchaseFrequency(BigDecimal.valueOf(Math.min(purchaseCount / 10.0, 1.0)));

            // 计算价格敏感度（基于购买商品的价格分析）
            List<UserBehavior> purchaseBehaviors = behaviors.stream()
                    .filter(b -> b.getBehaviorType() == 5)
                    .collect(Collectors.toList());
            
            if (!purchaseBehaviors.isEmpty()) {
                double avgPurchasePrice = 0.0;
                int validPrices = 0;
                
                for (UserBehavior behavior : purchaseBehaviors) {
                    try {
                        SeedInfo seedInfo = seedInfoMapper.selectById(behavior.getTargetId());
                        if (seedInfo != null && seedInfo.getUnitPrice() != null) {
                            avgPurchasePrice += seedInfo.getUnitPrice().doubleValue();
                            validPrices++;
                        }
                    } catch (Exception e) {
                        log.warn("获取商品价格失败，seedId: {}", behavior.getTargetId());
                    }
                }
                
                if (validPrices > 0) {
                    avgPurchasePrice = avgPurchasePrice / validPrices;
                    // 价格敏感度：购买的商品价格越低，价格敏感度越高
                    // 假设价格区间为0-200，计算敏感度
                    double sensitivity = Math.max(0.0, Math.min(1.0, (200.0 - avgPurchasePrice) / 200.0));
                    profile.setPriceSensitivity(BigDecimal.valueOf(sensitivity));
                } else {
                    profile.setPriceSensitivity(BigDecimal.valueOf(0.5)); // 默认中等敏感度
                }
            } else {
                profile.setPriceSensitivity(BigDecimal.valueOf(0.5)); // 默认中等敏感度
            }

            // 计算质量要求（基于评价行为分析）
            double avgRating = behaviors.stream()
                    .filter(b -> b.getBehaviorType() == 6 && b.getRating() != null)
                    .mapToDouble(UserBehavior::getRating)
                    .average()
                    .orElse(3.0);
            profile.setQualityRequirement(BigDecimal.valueOf(avgRating / 5.0));

            // 设置其他默认值
            if (profile.getUserType() == null) {
                profile.setUserType(1); // 默认个人用户
            }
            if (profile.getRegion() == null) {
                profile.setRegion("未知"); // 默认地区
            }
            if (profile.getBrandLoyalty() == null) {
                profile.setBrandLoyalty(BigDecimal.valueOf(0.5));
            }
            if (profile.getRecommendationWeight() == null) {
                // 基于活跃度和购买频率计算推荐权重
                double weight = (profile.getActivityLevel().doubleValue() + profile.getPurchaseFrequency().doubleValue()) / 2.0;
                profile.setRecommendationWeight(BigDecimal.valueOf(weight));
            }
            
            // 保存用户画像
            if (profile.getId() == null) {
                userProfileMapper.insert(profile);
            } else {
                userProfileMapper.updateById(profile);
            }

        } catch (Exception e) {
            log.error("更新用户画像失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateSeedFeatures(Long seedId) {
        try {
            // 1. 获取种子信息
            SeedInfo seedInfo = seedInfoMapper.selectById(seedId);
            if (seedInfo == null) {
                return;
            }

            // 2. 计算商品特征
            SeedFeatures features = seedFeaturesMapper.selectBySeedId(seedId);
            if (features == null) {
                features = new SeedFeatures();
                features.setId(IdGenerator.generateId());
                features.setSeedId(seedId);
            }

            // 价格特征（基于价格区间）
            BigDecimal priceFeature = calculatePriceFeature(seedInfo.getUnitPrice());
            features.setPriceFeature(priceFeature);

            // 质量特征（基于用户评分）
            BigDecimal qualityFeature = calculateQualityFeature(seedId);
            features.setQualityFeature(qualityFeature);

            // 品牌特征（基于品牌知名度）
            BigDecimal brandFeature = calculateBrandFeature(seedInfo.getVariety());
            features.setBrandFeature(brandFeature);

            // 市场热度（基于浏览和购买行为）
            BigDecimal marketHeat = calculateMarketHeat(seedId);
            features.setMarketHeat(marketHeat);

            // 用户评分
            BigDecimal userRating = calculateUserRating(seedId);
            features.setUserRating(userRating);

            // 推荐权重
            BigDecimal recommendationWeight = calculateRecommendationWeight(features);
            features.setRecommendationWeight(recommendationWeight);

            // 保存特征
            if (features.getId() == null) {
                seedFeaturesMapper.insert(features);
            } else {
                seedFeaturesMapper.updateById(features);
            }

        } catch (Exception e) {
            log.error("计算商品特征失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateRecommendations(Long userId) {
        try {
            // 1. 生成混合推荐
            List<RecommendationVO> recommendations = hybridRecommend(userId, 50);

            // 2. 保存推荐结果
            String batchId = IdUtil.fastSimpleUUID();
            for (RecommendationVO vo : recommendations) {
                Recommendation recommendation = new Recommendation();
                recommendation.setId(IdGenerator.generateId());
                recommendation.setUserId(userId);
                recommendation.setRecommendationType(vo.getRecommendationType());
                recommendation.setTargetType(1); // 种子
                recommendation.setTargetId(vo.getTargetId());
                recommendation.setRecommendationScore(vo.getRecommendationScore() == null ? new BigDecimal("0.0")
                        : vo.getRecommendationScore());
                recommendation.setRecommendationReason(vo.getRecommendationReason());
                recommendation.setRecommendationWeight(vo.getRecommendationWeight() == null ? new BigDecimal("1.0")
                        : vo.getRecommendationWeight());
                recommendation.setIsShown(0);
                recommendation.setIsClicked(0);
                recommendation.setIsPurchased(0);
                recommendation.setAlgorithmVersion("v1.0");
                recommendation.setBatchId(batchId);

                recommendationMapper.insert(recommendation);
            }

        } catch (Exception e) {
            log.error("生成推荐结果失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordRecommendationClick(Long recommendationId) {
        try {
            Recommendation recommendation = recommendationMapper.selectById(recommendationId);
            if (recommendation != null) {
                recommendation.setIsClicked(1);
                recommendation.setClickTime(LocalDateTime.now());
                recommendationMapper.updateById(recommendation);
            }
        } catch (Exception e) {
            log.error("记录推荐点击失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordRecommendationPurchase(Long recommendationId) {
        try {
            Recommendation recommendation = recommendationMapper.selectById(recommendationId);
            if (recommendation != null) {
                recommendation.setIsPurchased(1);
                recommendation.setPurchaseTime(LocalDateTime.now());
                recommendationMapper.updateById(recommendation);
            }
        } catch (Exception e) {
            log.error("记录推荐购买失败", e);
        }
    }

    /**
     * 分析用户偏好
     */
    private Map<Long, Double> analyzeUserPreferences(List<UserBehavior> behaviors) {
        Map<Long, Double> preferences = new HashMap<>();

        for (UserBehavior behavior : behaviors) {
            if (behavior.getBehaviorType() == 5 || behavior.getBehaviorType() == 6) { // 购买或评价
                Long targetId = behavior.getTargetId();
                Double weight = behavior.getBehaviorWeight();
                if (behavior.getRating() != null) {
                    weight *= behavior.getRating() / 5.0;
                }
                preferences.merge(targetId, weight, Double::sum);
            }
        }

        return preferences;
    }

    /**
     * 转换为推荐结果
     */
    private List<RecommendationVO> convertToRecommendations(List<SeedFeatures> features, Integer type, String reason) {
        return features.stream()
                .map(f -> {
                    RecommendationVO vo = new RecommendationVO();
                    vo.setTargetId(f.getSeedId());
                    BigDecimal score = f.getRecommendationWeight() != null ? f.getRecommendationWeight()
                            : new BigDecimal("0.5");
                    vo.setRecommendationScore(score);
                    vo.setRecommendationType(type);
                    vo.setRecommendationTypeName(getRecommendationTypeName(type));
                    vo.setRecommendationReason(reason);
                    // 默认权重，避免空值引发持久化异常
                    vo.setRecommendationWeight(new BigDecimal("1.0"));
                    // 填充商品信息
                    fillProductInfo(vo, f.getSeedId());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取推荐类型名称
     */
    private String getRecommendationTypeName(Integer type) {
        switch (type) {
            case 1:
                return "协同过滤";
            case 2:
                return "内容推荐";
            case 3:
                return "热门推荐";
            case 4:
                return "个性化推荐";
            default:
                return "未知";
        }
    }

    /**
     * 计算价格特征
     */
    private BigDecimal calculatePriceFeature(BigDecimal price) {
        // 简化的价格特征计算
        if (price.compareTo(new BigDecimal("10")) < 0)
            return new BigDecimal("0.2");
        if (price.compareTo(new BigDecimal("50")) < 0)
            return new BigDecimal("0.5");
        if (price.compareTo(new BigDecimal("100")) < 0)
            return new BigDecimal("0.8");
        return new BigDecimal("1.0");
    }

    /**
     * 计算质量特征
     */
    private BigDecimal calculateQualityFeature(Long seedId) {
        // 基于用户评价计算质量特征
        List<UserBehavior> ratings = userBehaviorMapper.selectByTargetIdAndType(seedId, 1, 6);
        if (CollUtil.isEmpty(ratings)) {
            return new BigDecimal("0.5");
        }

        double avgRating = ratings.stream()
                .mapToDouble(b -> b.getRating() != null ? b.getRating() : 3.0)
                .average()
                .orElse(3.0);

        return new BigDecimal(avgRating / 5.0);
    }

    /**
     * 计算品牌特征
     */
    private BigDecimal calculateBrandFeature(String brand) {
        // 简化的品牌特征计算
        if (StrUtil.isBlank(brand))
            return new BigDecimal("0.3");
        if (brand.contains("知名") || brand.contains("大牌"))
            return new BigDecimal("0.9");
        return new BigDecimal("0.6");
    }

    /**
     * 计算市场热度
     */
    private BigDecimal calculateMarketHeat(Long seedId) {
        // 基于浏览和购买行为计算市场热度
        List<UserBehavior> behaviors = userBehaviorMapper.selectByTargetId(seedId, 1, 1000);
        long viewCount = behaviors.stream().filter(b -> b.getBehaviorType() == 1).count();
        long purchaseCount = behaviors.stream().filter(b -> b.getBehaviorType() == 5).count();

        double heat = (viewCount * 0.1 + purchaseCount * 0.9) / 100.0;
        return new BigDecimal(Math.min(heat, 1.0));
    }

    /**
     * 计算用户评分
     */
    private BigDecimal calculateUserRating(Long seedId) {
        return calculateQualityFeature(seedId);
    }

    /**
     * 计算推荐权重
     */
    private BigDecimal calculateRecommendationWeight(SeedFeatures features) {
        // 综合多个特征计算推荐权重
        double weight = features.getMarketHeat().doubleValue() * 0.3 +
                features.getUserRating().doubleValue() * 0.4 +
                features.getQualityFeature().doubleValue() * 0.3;
        return new BigDecimal(Math.min(weight, 1.0));
    }

    /**
     * 计算用户相似度
     */
    private Map<Long, Double> calculateUserSimilarities(Long userId, List<UserBehavior> userBehaviors, List<UserProfile> candidateProfiles) {
        Map<Long, Double> similarities = new HashMap<>();
        
        // 获取当前用户的行为特征向量
        Map<Long, Double> userItemRatings = getUserItemRatings(userBehaviors);
        
        for (UserProfile candidate : candidateProfiles) {
            if (candidate.getUserId().equals(userId)) {
                continue; // 跳过自己
            }
            
            // 获取候选用户的行为记录
            List<UserBehavior> candidateBehaviors = userBehaviorMapper.selectByUserId(candidate.getUserId(), 100);
            Map<Long, Double> candidateItemRatings = getUserItemRatings(candidateBehaviors);
            
            // 计算余弦相似度
            double similarity = calculateCosineSimilarity(userItemRatings, candidateItemRatings);
            
            // 结合用户画像相似度
            double profileSimilarity = calculateProfileSimilarity(getUserProfile(userId), candidate);
            
            // 综合相似度（行为相似度权重0.7，画像相似度权重0.3）
            double finalSimilarity = similarity * 0.7 + profileSimilarity * 0.3;
            
            if (finalSimilarity > 0) {
                similarities.put(candidate.getUserId(), finalSimilarity);
            }
        }
        
        return similarities;
    }
    
    /**
     * 获取用户对商品的评分向量
     */
    private Map<Long, Double> getUserItemRatings(List<UserBehavior> behaviors) {
        Map<Long, Double> ratings = new HashMap<>();
        
        for (UserBehavior behavior : behaviors) {
            Long itemId = behavior.getTargetId();
            double rating = 0.0;
            
            // 根据行为类型计算隐式评分
            switch (behavior.getBehaviorType()) {
                case 1: // 浏览
                    rating = 1.0;
                    break;
                case 2: // 搜索
                    rating = 1.5;
                    break;
                case 3: // 收藏
                    rating = 3.0;
                    break;
                case 4: // 加购物车
                    rating = 3.5;
                    break;
                case 5: // 购买
                    rating = 5.0;
                    break;
                case 6: // 评价
                    rating = behavior.getRating() != null ? behavior.getRating().doubleValue() : 3.0;
                    break;
                default:
                    rating = 1.0;
            }
            
            // 考虑行为权重和时间衰减
            Double behaviorWeight = behavior.getBehaviorWeight() != null ? behavior.getBehaviorWeight() : 1.0;
            double timeDecay = calculateTimeDecay(behavior.getBehaviorTime());
            
            double finalRating = rating * behaviorWeight * timeDecay;
            ratings.merge(itemId, finalRating, Double::sum);
        }
        
        return ratings;
    }
    
    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(Map<Long, Double> ratingsA, Map<Long, Double> ratingsB) {
        Set<Long> commonItems = new HashSet<>(ratingsA.keySet());
        commonItems.retainAll(ratingsB.keySet());
        
        if (commonItems.isEmpty()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (Long itemId : commonItems) {
            double ratingA = ratingsA.get(itemId);
            double ratingB = ratingsB.get(itemId);
            
            dotProduct += ratingA * ratingB;
            normA += ratingA * ratingA;
            normB += ratingB * ratingB;
        }
        
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    /**
     * 计算用户画像相似度
     */
    private double calculateProfileSimilarity(UserProfile profileA, UserProfile profileB) {
        if (profileA == null || profileB == null) {
            return 0.0;
        }
        
        double similarity = 0.0;
        int factors = 0;
        
        // 用户类型相似度
        if (Objects.equals(profileA.getUserType(), profileB.getUserType())) {
            similarity += 0.3;
        }
        factors++;
        
        // 地区相似度
        if (Objects.equals(profileA.getRegion(), profileB.getRegion())) {
            similarity += 0.2;
        }
        factors++;
        
        // 价格敏感度相似度
        if (profileA.getPriceSensitivity() != null && profileB.getPriceSensitivity() != null) {
            double priceDiff = Math.abs(profileA.getPriceSensitivity().doubleValue() - profileB.getPriceSensitivity().doubleValue());
            similarity += (1.0 - priceDiff) * 0.2;
            factors++;
        }
        
        // 质量要求相似度
        if (profileA.getQualityRequirement() != null && profileB.getQualityRequirement() != null) {
            double qualityDiff = Math.abs(profileA.getQualityRequirement().doubleValue() - profileB.getQualityRequirement().doubleValue());
            similarity += (1.0 - qualityDiff) * 0.2;
            factors++;
        }
        
        // 购买偏好相似度
        if (Objects.equals(profileA.getPurchasePreference(), profileB.getPurchasePreference())) {
            similarity += 0.1;
        }
        factors++;
        
        return factors > 0 ? similarity / factors : 0.0;
    }
    
    /**
     * 计算时间衰减因子
     */
    private double calculateTimeDecay(LocalDateTime behaviorTime) {
        if (behaviorTime == null) {
            return 0.5;
        }
        
        long daysDiff = java.time.Duration.between(behaviorTime, LocalDateTime.now()).toDays();
        
        // 时间衰减：30天内权重为1，之后每30天衰减一半
        if (daysDiff <= 30) {
            return 1.0;
        } else if (daysDiff <= 60) {
            return 0.8;
        } else if (daysDiff <= 90) {
            return 0.6;
        } else if (daysDiff <= 180) {
            return 0.4;
        } else {
            return 0.2;
        }
    }
    
    /**
     * 获取用户画像（带缓存）
     */
    private UserProfile getUserProfile(Long userId) {
        return userProfileMapper.selectByUserId(userId);
    }

    /**
     * 填充商品信息到推荐VO
     */
    private void fillProductInfo(RecommendationVO vo, Long seedId) {
        try {
            SeedInfo seedInfo = seedInfoMapper.selectById(seedId);
            if (seedInfo != null) {
                vo.setTargetName(seedInfo.getSeedName());
                vo.setTargetImage(seedInfo.getImageUrl());
                vo.setTargetPrice(seedInfo.getUnitPrice());
            }
        } catch (Exception e) {
            log.warn("填充商品信息失败，seedId: {}", seedId, e);
        }
    }
}
