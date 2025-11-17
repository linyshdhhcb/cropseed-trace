package com.linyi.cropseed.trace.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.linyi.cropseed.trace.common.util.IdGenerator;
import com.linyi.cropseed.trace.config.RecommendationConfig;
import com.linyi.cropseed.trace.dto.Recommend.ContentPreferenceDTO;
import com.linyi.cropseed.trace.dto.Recommend.HybridRecommendationItemDTO;
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
import java.time.temporal.ChronoUnit;
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
public class RecommendationServiceImpl implements RecommendationService {

    private final UserProfileMapper userProfileMapper;
    private final UserBehaviorMapper userBehaviorMapper;
    private final SeedFeaturesMapper seedFeaturesMapper;
    private final SeedInfoMapper seedInfoMapper;
    private final RecommendationMapper recommendationMapper;
    private final RecommendationConfig recommendationConfig;

    @Override
    public List<RecommendationVO> collaborativeFilteringRecommend(Long userId, Integer limit) {
        try {
            // 获取用户画像
            UserProfile userProfile = userProfileMapper.selectByUserId(userId);
            if (userProfile == null) {
                log.warn("用户画像不存在，userId: {}", userId);
                return popularRecommend(limit);
            }

            // 获取用户历史行为
            List<UserBehavior> userBehaviors = userBehaviorMapper.selectByUserId(userId, 200);
            if (CollUtil.isEmpty(userBehaviors)) {
                log.info("用户无历史行为记录，返回热门推荐，userId: {}", userId);
                return popularRecommend(limit);
            }

            //  找到相似用户候选集
            List<UserProfile> candidateProfiles = userProfileMapper.selectSimilarProfiles(
                    userProfile.getUserType(), userProfile.getRegion(), 30);

            if (CollUtil.isEmpty(candidateProfiles)) {
                log.warn("未找到相似用户，userId: {}", userId);
                return popularRecommend(limit);
            }

            //  批量获取候选用户的行为记录（优化N+1查询问题）
            List<Long> candidateUserIds = candidateProfiles.stream()
                    .map(UserProfile::getUserId)
                    .filter(id -> !id.equals(userId)) // 排除自己
                    .collect(Collectors.toList());

            Map<Long, List<UserBehavior>> candidateBehaviorsMap = batchGetUserBehaviors(candidateUserIds);

            //  计算用户相似度
            Map<Long, Double> userSimilarities = calculateUserSimilaritiesOptimized(userId, userBehaviors, candidateBehaviorsMap, candidateProfiles);

            // 筛选高相似度用户
            List<Long> similarUserIds = userSimilarities.entrySet().stream()
                    .filter(entry -> entry.getValue() > recommendationConfig.getSimilarityThreshold())
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(15) // 增加候选用户数量以提高推荐多样性
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (similarUserIds.isEmpty()) {
                log.warn("未找到高相似度用户，userId: {}", userId);
                return popularRecommend(limit);
            }

            //  聚合相似用户的行为，计算商品推荐评分
            Map<Long, Double> itemScores = calculateItemScores(userBehaviors, candidateBehaviorsMap, similarUserIds, userSimilarities);

            //  过滤已购买商品（可配置是否完全过滤或降权）
            Set<Long> userPurchasedItems = getUserPurchasedItems(userBehaviors);

            //  排序并返回推荐结果
            return itemScores.entrySet().stream()
                    .filter(entry -> !userPurchasedItems.contains(entry.getKey())) // 过滤已购买商品
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> {
                        RecommendationVO vo = new RecommendationVO();
                        vo.setTargetId(entry.getKey());
                        vo.setRecommendationScore(BigDecimal.valueOf(entry.getValue()));
                        vo.setRecommendationType(1);
                        vo.setRecommendationTypeName("协同过滤");
                        vo.setRecommendationReason("基于相似用户偏好推荐");
                        fillProductInfo(vo, entry.getKey());
                        return vo;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("协同过滤推荐失败，userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecommendationVO> contentBasedRecommend(Long userId, Integer limit) {
        try {
            //  获取用户历史行为
            List<UserBehavior> userBehaviors = userBehaviorMapper.selectByUserId(userId, 200);
            if (CollUtil.isEmpty(userBehaviors)) {
                log.warn("用户无历史行为，无法进行内容推荐，userId: {}", userId);
                return Collections.emptyList();
            }

            //  分析用户对商品特征的偏好
            ContentPreferenceDTO contentPreference = analyzeContentPreference(userBehaviors);

            //  根据用户偏好查找相似商品（优化查询策略）
            // 暂时简化实现，使用现有的查询方法
            List<SeedFeatures> candidateFeatures = seedFeaturesMapper.selectPopularFeatures(0.5, limit * 3);

            if (CollUtil.isEmpty(candidateFeatures)) {
                log.warn("未找到匹配用户偏好的商品，userId: {}", userId);
                return Collections.emptyList();
            }

            //  计算内容相似度并排序
            return candidateFeatures.stream()
                .filter(features -> !getUserPurchasedItems(userBehaviors).contains(features.getSeedId())) // 过滤已购买
                .map(features -> {
                    double similarity = calculateContentSimilarity(contentPreference, features);

                    RecommendationVO vo = new RecommendationVO();
                    vo.setTargetId(features.getSeedId());
                    vo.setRecommendationScore(BigDecimal.valueOf(similarity));
                    vo.setRecommendationType(2);
                    vo.setRecommendationTypeName("内容推荐");
                    vo.setRecommendationReason("基于商品特征相似性推荐");
                    fillProductInfo(vo, features.getSeedId());
                    return vo;
                })
                .sorted((a, b) -> b.getRecommendationScore().compareTo(a.getRecommendationScore()))
                .limit(limit)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("内容推荐失败，userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecommendationVO> popularRecommend(Integer limit) {
        try {
            //  获取热门商品特征
            List<SeedFeatures> popularFeatures = seedFeaturesMapper.selectPopularFeatures(0.5, limit * 2);

            //  转换为推荐结果并填充商品信息
            return popularFeatures.stream()
                    .map(features -> {
                        RecommendationVO vo = new RecommendationVO();
                        vo.setTargetId(features.getSeedId());
                        vo.setRecommendationScore(features.getMarketHeat());
                        vo.setRecommendationType(3);
                        vo.setRecommendationTypeName("热门推荐");
                        vo.setRecommendationReason("基于市场热度商品推荐");
                        vo.setRecommendationWeight(BigDecimal.valueOf(recommendationConfig.getHybridWeight().getPopular()));
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
            //  获取用户画像和历史行为
            UserProfile userProfile = userProfileMapper.selectByUserId(userId);
            if (userProfile == null) {
                log.warn("用户画像不存在，userId: {}", userId);
                return Collections.emptyList();
            }

            List<UserBehavior> userBehaviors = userBehaviorMapper.selectByUserId(userId, 100);
            Set<Long> userPurchasedItems = getUserPurchasedItems(userBehaviors);

            //  多维度个性化推荐
            List<RecommendationVO> recommendations = new ArrayList<>();

            //  价格敏感度推荐
            if (userProfile.getPriceSensitivity() != null && userProfile.getPriceSensitivity().doubleValue() > 0.6) {
                List<SeedFeatures> priceFeatures = seedFeaturesMapper.selectPriceRangeFeatures(0.0, 0.4, limit);
                recommendations.addAll(convertToRecommendationsWithFilter(priceFeatures, 4, "基于价格敏感度推荐", userPurchasedItems));
            }

            //  质量要求推荐
            if (userProfile.getQualityRequirement() != null && userProfile.getQualityRequirement().doubleValue() > 0.6) {
                List<SeedFeatures> qualityFeatures = seedFeaturesMapper.selectHighQualityFeatures(0.6, limit);
                recommendations.addAll(convertToRecommendationsWithFilter(qualityFeatures, 4, "基于质量要求推荐", userPurchasedItems));
            }

            //  活跃度推荐（活跃用户推荐新品）
            if (userProfile.getActivityLevel() != null && userProfile.getActivityLevel().doubleValue() > 0.7) {
                // 暂时简化实现，使用热门推荐代替
                List<SeedFeatures> newFeatures = seedFeaturesMapper.selectPopularFeatures(0.6, limit);
                recommendations.addAll(convertToRecommendationsWithFilter(newFeatures, 4, "基于活跃度新品推荐", userPurchasedItems));
            }

            //  品牌忠诚度推荐
            if (userProfile.getBrandLoyalty() != null && userProfile.getBrandLoyalty().doubleValue() > 0.5) {
                // 暂时简化实现，使用高质量推荐代替
                List<SeedFeatures> brandFeatures = seedFeaturesMapper.selectHighQualityFeatures(0.8, limit);
                recommendations.addAll(convertToRecommendationsWithFilter(brandFeatures, 4, "基于品牌忠诚度推荐", userPurchasedItems));
            }

            //  去重、权重计算并排序
            return recommendations.stream()
                    .collect(Collectors.toMap(
                            RecommendationVO::getTargetId,
                            vo -> vo,
                            (existing, replacement) -> {
                                // 选择评分更高的
                                return existing.getRecommendationScore().compareTo(replacement.getRecommendationScore()) > 0 ? existing : replacement;
                            }))
                    .values()
                    .stream()
                    .sorted((a, b) -> b.getRecommendationScore().compareTo(a.getRecommendationScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("个性化推荐失败，userId: {}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<RecommendationVO> hybridRecommend(Long userId, Integer limit) {
        try {
            log.info("开始混合推荐，userId: {}, limit: {}", userId, limit);

            // 参数校验
            if (userId == null || limit == null || limit <= 0) {
                log.warn("混合推荐参数无效，userId: {}, limit: {}", userId, limit);
                return Collections.emptyList();
            }

            // 并行获取各类推荐结果（优化性能）
            CompletableFuture<List<RecommendationVO>> cfFuture = CompletableFuture.supplyAsync(() ->
                collaborativeFilteringRecommend(userId, Math.max(10, limit / 2))); // 增加候选数量

            CompletableFuture<List<RecommendationVO>> cbFuture = CompletableFuture.supplyAsync(() ->
                contentBasedRecommend(userId, Math.max(10, limit / 2)));

            CompletableFuture<List<RecommendationVO>> personalizedFuture = CompletableFuture.supplyAsync(() ->
                personalizedRecommend(userId, Math.max(10, limit / 2)));

            CompletableFuture<List<RecommendationVO>> popularFuture = CompletableFuture.supplyAsync(() ->
                popularRecommend(Math.max(10, limit / 2)));

            // 等待所有推荐结果（带超时和异常处理）
            List<RecommendationVO> cfRecommendations = safeGetRecommendations(cfFuture, "协同过滤");
            List<RecommendationVO> cbRecommendations = safeGetRecommendations(cbFuture, "内容推荐");
            List<RecommendationVO> personalizedRecommendations = safeGetRecommendations(personalizedFuture, "个性化推荐");
            List<RecommendationVO> popularRecommendations = safeGetRecommendations(popularFuture, "热门推荐");

            log.info("各类推荐数量 - 协同过滤:{}, 内容:{}, 个性化:{}, 热门:{}",
                cfRecommendations.size(), cbRecommendations.size(),
                personalizedRecommendations.size(), popularRecommendations.size());

            //  正确的混合推荐算法（融合多算法评分）
            Map<Long, HybridRecommendationItemDTO> hybridScores = new HashMap<>();

            // 按照权重积累各算法的评分
            processAlgorithmResults(hybridScores, cfRecommendations, recommendationConfig.getHybridWeight().getCollaborative(), "协同过滤");
            processAlgorithmResults(hybridScores, cbRecommendations, recommendationConfig.getHybridWeight().getContent(), "内容推荐");
            processAlgorithmResults(hybridScores, personalizedRecommendations, recommendationConfig.getHybridWeight().getPersonalized(), "个性化推荐");
            processAlgorithmResults(hybridScores, popularRecommendations, recommendationConfig.getHybridWeight().getPopular(), "热门推荐");

            //  按照最终评分排序并返回结果
            if (hybridScores.isEmpty()) {
                log.warn("所有推荐算法都没有结果，使用备选热门推荐，userId: {}", userId);
                return popularRecommend(limit);
            }

            List<RecommendationVO> finalResults = hybridScores.values().stream()
                    .filter(item -> item.getFinalScore() > 0) // 过滤评分为0的结果
                    .sorted((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()))
                    .limit(limit)
                    .map(item -> {
                        RecommendationVO vo = item.getRecommendationVO();
                        vo.setRecommendationScore(BigDecimal.valueOf(Math.max(0.0, item.getFinalScore()))); // 确保评分不为负
                        vo.setRecommendationType(99); // 混合推荐类型
                        vo.setRecommendationTypeName("混合推荐");
                        vo.setRecommendationReason("混合推荐:" + item.getReason());
                        return vo;
                    })
                    .collect(Collectors.toList());

            // 如果混合推荐结果不足，补充热门推荐
            if (finalResults.size() < limit) {
                int remaining = limit - finalResults.size();
                List<RecommendationVO> supplementary = popularRecommend(remaining * 2);
                Set<Long> existingIds = finalResults.stream().map(RecommendationVO::getTargetId).collect(Collectors.toSet());

                supplementary.stream()
                    .filter(vo -> !existingIds.contains(vo.getTargetId()))
                    .limit(remaining)
                    .forEach(finalResults::add);
            }

            log.info("混合推荐最终结果数量: {}, 参与计算商品总数: {}", finalResults.size(), hybridScores.size());
            if (!finalResults.isEmpty()) {
                log.info("推荐结果前3个: {}",
                    finalResults.stream().limit(3)
                        .map(r -> String.format("ID:%d,评分:%.4f,理由:%s",
                            r.getTargetId(), r.getRecommendationScore().doubleValue(), r.getRecommendationReason()))
                        .collect(Collectors.joining("; ")));
            }

            return finalResults;

        } catch (Exception e) {
            log.error("混合推荐失败，降级为热门推荐，userId: {}, limit: {}", userId, limit, e);
            // 异常情况下返回热门推荐作为备选方案
            try {
                return popularRecommend(limit);
            } catch (Exception fallbackException) {
                log.error("备选热门推荐也失败，userId: {}", userId, fallbackException);
                return Collections.emptyList();
            }
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
                    log.info("开始异步更新用户画像，userId: {}", userId);
                    updateUserProfile(userId);
                    log.info("用户画像更新完成，userId: {}", userId);
                } catch (Exception e) {
                    log.error("异步更新用户画像失败，userId: {}", userId, e);
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
            log.info("开始更新用户画像，userId: {}", userId);

            //  获取用户行为统计
            List<UserBehavior> behaviors = userBehaviorMapper.selectByUserId(userId, 1000);
            log.info("获取到用户行为数据，userId: {}, 行为数量: {}", userId, behaviors.size());

            if (CollUtil.isEmpty(behaviors)) {
                log.warn("用户没有行为数据，跳过画像更新，userId: {}", userId);
                return;
            }

            //  计算用户画像指标
            UserProfile profile = userProfileMapper.selectByUserId(userId);
            boolean isNewProfile = false;
            if (profile == null) {
                log.info("创建新的用户画像，userId: {}", userId);
                profile = new UserProfile();
                profile.setId(IdGenerator.generateId());
                profile.setUserId(userId);
                isNewProfile = true;
            } else {
                log.info("更新已存在的用户画像，userId: {}, profileId: {}", userId, profile.getId());
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
            log.info("准备保存用户画像，userId: {}, isNewProfile: {}", userId, isNewProfile);
            log.info("用户画像数据 - 活跃度: {}, 购买频率: {}, 价格敏感度: {}, 质量要求: {}",
                    profile.getActivityLevel(), profile.getPurchaseFrequency(),
                    profile.getPriceSensitivity(), profile.getQualityRequirement());

            if (isNewProfile) {
                int insertResult = userProfileMapper.insert(profile);
                log.info("插入新用户画像结果，userId: {}, insertResult: {}", userId, insertResult);
            } else {
                int updateResult = userProfileMapper.updateById(profile);
                log.info("更新用户画像结果，userId: {}, updateResult: {}", userId, updateResult);
            }

            log.info("用户画像保存完成，userId: {}", userId);

        } catch (Exception e) {
            log.error("更新用户画像失败，userId: {}", userId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateSeedFeatures(Long seedId) {
        try {
            //  获取种子信息
            SeedInfo seedInfo = seedInfoMapper.selectById(seedId);
            if (seedInfo == null) {
                return;
            }

            // 计算商品特征
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
            // 生成混合推荐
            List<RecommendationVO> recommendations = hybridRecommend(userId, 50);

            //  保存推荐结果
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

            // 综合相似度（使用配置的权重）
            double finalSimilarity = similarity * recommendationConfig.getSimilarityWeight().getBehavior() +
                                    profileSimilarity * recommendationConfig.getSimilarityWeight().getProfile();

            if (finalSimilarity > 0) {
                similarities.put(candidate.getUserId(), finalSimilarity);
            }
        }

        return similarities;
    }

    /**
     * 获取用户对商品的评分向量（使用配置参数）
     */
    private Map<Long, Double> getUserItemRatings(List<UserBehavior> behaviors) {
        Map<Long, Double> ratings = new HashMap<>();

        for (UserBehavior behavior : behaviors) {
            Long itemId = behavior.getTargetId();

            // 使用配置的行为权重
            double baseScore = recommendationConfig.getBehaviorWeight().getWeightByType(behavior.getBehaviorType());

            // 对于评价行为，使用实际评分
            if (behavior.getBehaviorType() == 6 && behavior.getRating() != null) {
                baseScore = behavior.getRating().doubleValue();
            }

            // 考虑行为权重和时间衰减
            Double behaviorWeight = behavior.getBehaviorWeight() != null ? behavior.getBehaviorWeight() : 1.0;
            double timeDecay = calculateTimeDecay(behavior.getBehaviorTime());

            double finalRating = baseScore * behaviorWeight * timeDecay;
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
     * 计算时间衰减因子（使用配置参数）
     */
    private double calculateTimeDecay(LocalDateTime behaviorTime) {
        if (behaviorTime == null) {
            return 0.2; // 默认最低权重
        }

        long daysBetween = ChronoUnit.DAYS.between(behaviorTime, LocalDateTime.now());
        int timeDecayDays = recommendationConfig.getTimeDecayDays();

        if (daysBetween <= timeDecayDays) {
            return 1.0;
        } else if (daysBetween <= timeDecayDays * 2) {
            return 0.8;
        } else if (daysBetween <= timeDecayDays * 3) {
            return 0.6;
        } else if (daysBetween <= timeDecayDays * 6) {
            return 0.4;
        } else {
            return 0.2;
        }
    }

    /**
     * 批量获取用户行为记录（解决N+1查询问题）
     */
    private Map<Long, List<UserBehavior>> batchGetUserBehaviors(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return new HashMap<>();
        }

        Map<Long, List<UserBehavior>> result = new HashMap<>();

        // 批量查询，每次最多100个用户
        int batchSize = 100;
        for (int i = 0; i < userIds.size(); i += batchSize) {
            List<Long> batchIds = userIds.subList(i, Math.min(i + batchSize, userIds.size()));

            // 这里需要在Mapper中添加批量查询方法
            List<UserBehavior> behaviors = userBehaviorMapper.selectByUserIds(batchIds, 50);

            // 按用户ID分组
            Map<Long, List<UserBehavior>> batchResult = behaviors.stream()
                    .collect(Collectors.groupingBy(UserBehavior::getUserId));
            result.putAll(batchResult);
        }

        return result;
    }

    /**
     * 优化的用户相似度计算（避免重复数据库查询）
     */
    private Map<Long, Double> calculateUserSimilaritiesOptimized(Long userId, List<UserBehavior> userBehaviors,
                                                                Map<Long, List<UserBehavior>> candidateBehaviorsMap,
                                                                List<UserProfile> candidateProfiles) {
        Map<Long, Double> similarities = new HashMap<>();

        // 获取当前用户的行为特征向量
        Map<Long, Double> userItemRatings = getUserItemRatings(userBehaviors);
        UserProfile userProfile = getUserProfile(userId);

        for (UserProfile candidate : candidateProfiles) {
            if (candidate.getUserId().equals(userId)) {
                continue;
            }

            List<UserBehavior> candidateBehaviors = candidateBehaviorsMap.getOrDefault(candidate.getUserId(), Collections.emptyList());
            if (candidateBehaviors.isEmpty()) {
                continue;
            }

            Map<Long, Double> candidateItemRatings = getUserItemRatings(candidateBehaviors);

            // 计算余弦相似度
            double behaviorSimilarity = calculateCosineSimilarity(userItemRatings, candidateItemRatings);

            // 计算画像相似度
            double profileSimilarity = calculateProfileSimilarity(userProfile, candidate);

            // 综合相似度
            double finalSimilarity = behaviorSimilarity * recommendationConfig.getSimilarityWeight().getBehavior() +
                                    profileSimilarity * recommendationConfig.getSimilarityWeight().getProfile();

            if (finalSimilarity > 0) {
                similarities.put(candidate.getUserId(), finalSimilarity);
            }
        }

        return similarities;
    }

    /**
     * 计算商品推荐评分
     */
    private Map<Long, Double> calculateItemScores(List<UserBehavior> userBehaviors,
                                                 Map<Long, List<UserBehavior>> candidateBehaviorsMap,
                                                 List<Long> similarUserIds,
                                                 Map<Long, Double> userSimilarities) {
        Map<Long, Double> itemScores = new HashMap<>();

        // 获取用户最近购买的商品（用于降权）
        Set<Long> recentPurchasedItems = getRecentPurchasedItems(userBehaviors);

        for (Long similarUserId : similarUserIds) {
            List<UserBehavior> behaviors = candidateBehaviorsMap.get(similarUserId);
            if (behaviors == null) continue;

            Double userSimilarity = userSimilarities.get(similarUserId);

            for (UserBehavior behavior : behaviors) {
                if (behavior.getBehaviorType() < 1 || behavior.getBehaviorType() > 6) {
                    continue;
                }

                Long targetId = behavior.getTargetId();

                // 计算综合评分
                double baseScore = recommendationConfig.getBehaviorWeight().getWeightByType(behavior.getBehaviorType());
                Double behaviorWeight = behavior.getBehaviorWeight() != null ? behavior.getBehaviorWeight() : 1.0;
                double timeDecay = calculateTimeDecay(behavior.getBehaviorTime());

                // 对于评价行为，使用实际评分
                if (behavior.getBehaviorType() == 6 && behavior.getRating() != null) {
                    baseScore = behavior.getRating().doubleValue();
                }

                Double score = baseScore * behaviorWeight * timeDecay * userSimilarity;

                // 对最近购买的商品降权
                if (recommendationConfig.getDowngrade().getEnabled() && recentPurchasedItems.contains(targetId)) {
                    score *= recommendationConfig.getDowngrade().getRecentPurchaseRatio();
                }

                itemScores.merge(targetId, score, Double::sum);
            }
        }

        return itemScores;
    }

    /**
     * 获取用户已购买的商品ID集合
     */
    private Set<Long> getUserPurchasedItems(List<UserBehavior> userBehaviors) {
        return userBehaviors.stream()
                .filter(b -> b.getBehaviorType() == 5) // 购买行为
                .map(UserBehavior::getTargetId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取用户最近购买的商品（用于降权处理）
     */
    private Set<Long> getRecentPurchasedItems(List<UserBehavior> userBehaviors) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(recommendationConfig.getRecentPurchaseDays());
        return userBehaviors.stream()
                .filter(b -> b.getBehaviorType() == 5 && b.getBehaviorTime().isAfter(cutoffTime))
                .map(UserBehavior::getTargetId)
                .collect(Collectors.toSet());
    }


    /**
     * 分析用户内容偏好
     */
    private ContentPreferenceDTO analyzeContentPreference(List<UserBehavior> userBehaviors) {
        // 根据用户行为分析内容偏好，这里简化实现
        return new ContentPreferenceDTO(0.5, 0.7, Arrays.asList("知名品牌"));
    }

    /**
     * 计算内容相似度
     */
    private double calculateContentSimilarity(ContentPreferenceDTO preference, SeedFeatures features) {
        double priceScore = 1.0 - Math.abs(preference.getAvgPriceRange() - features.getPriceFeature().doubleValue());
        double qualityScore = 1.0 - Math.abs(preference.getAvgQualityRange() - features.getQualityFeature().doubleValue());
        String brandFeature = features.getBrandFeature() != null ? features.getBrandFeature().toString() : "";
        double brandScore = preference.getPreferredBrands().contains(brandFeature) ? 1.0 : 0.5;

        return (priceScore * 0.4 + qualityScore * 0.4 + brandScore * 0.2) * features.getMarketHeat().doubleValue();
    }

    /**
     * 转换商品特征为推荐结果（过滤已购买商品）
     */
    private List<RecommendationVO> convertToRecommendationsWithFilter(List<SeedFeatures> features, int type, String reason, Set<Long> excludeIds) {
        return features.stream()
                .filter(f -> !excludeIds.contains(f.getSeedId())) // 过滤已购买
                .map(feature -> {
                    RecommendationVO vo = new RecommendationVO();
                    vo.setTargetId(feature.getSeedId());
                    vo.setRecommendationScore(feature.getRecommendationWeight());
                    vo.setRecommendationType(type);
                    vo.setRecommendationTypeName("个性化推荐");
                    vo.setRecommendationReason(reason);
                    fillProductInfo(vo, feature.getSeedId());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 处理单个算法的推荐结果
     */
    private void processAlgorithmResults(Map<Long, HybridRecommendationItemDTO> hybridScores,
                                        List<RecommendationVO> results,
                                        Double weight,
                                        String algorithmName) {
        for (RecommendationVO vo : results) {
            Long targetId = vo.getTargetId();
            double score = vo.getRecommendationScore() != null ? vo.getRecommendationScore().doubleValue() : 0.0;

            HybridRecommendationItemDTO item = hybridScores.computeIfAbsent(targetId,
                k -> new HybridRecommendationItemDTO(vo));

            item.addAlgorithmScore(algorithmName, weight, score);
        }
    }

    /**
     * 安全获取推荐结果（带异常处理）
     */
    private List<RecommendationVO> safeGetRecommendations(CompletableFuture<List<RecommendationVO>> future, String algorithmName) {
        try {
            List<RecommendationVO> result = future.join();
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.warn("{}算法执行异常，跳过该算法", algorithmName, e);
            return Collections.emptyList();
        }
    }

    /**
     * 提取用户偏好品牌
     */
    @SuppressWarnings("unused")
    private String extractPreferredBrand(List<UserBehavior> userBehaviors) {
        // 根据用户购买历史分析偏好品牌，这里简化实现
        return "知名品牌";
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
