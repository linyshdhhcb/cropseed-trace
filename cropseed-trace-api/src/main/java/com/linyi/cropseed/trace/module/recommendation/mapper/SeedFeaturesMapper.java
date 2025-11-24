package com.linyi.cropseed.trace.module.recommendation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.module.recommendation.model.entity.SeedFeatures;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品特征Mapper
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface SeedFeaturesMapper extends BaseMapper<SeedFeatures> {

    /**
     * 根据种子ID查询特征
     */
    @Select("SELECT * FROM seed_features WHERE seed_id = #{seedId} AND deleted_flag = 0")
    SeedFeatures selectBySeedId(Long seedId);

    /**
     * 查询相似特征的商品
     */
    @Select("SELECT * FROM seed_features WHERE seed_id != #{seedId} AND deleted_flag = 0 " +
            "ORDER BY ABS(price_feature - #{priceFeature}) + ABS(quality_feature - #{qualityFeature}) + " +
            "ABS(brand_feature - #{brandFeature}) ASC LIMIT #{limit}")
    List<SeedFeatures> selectSimilarFeatures(@Param("seedId") Long seedId,
            @Param("priceFeature") Double priceFeature,
            @Param("qualityFeature") Double qualityFeature,
            @Param("brandFeature") Double brandFeature,
            @Param("limit") Integer limit);

    /**
     * 查询热门商品特征
     */
    @Select("SELECT * FROM seed_features WHERE market_heat > #{minHeat} AND deleted_flag = 0 " +
            "ORDER BY market_heat DESC, recommendation_weight DESC LIMIT #{limit}")
    List<SeedFeatures> selectPopularFeatures(@Param("minHeat") Double minHeat, @Param("limit") Integer limit);

    /**
     * 查询高质量商品特征
     */
    @Select("SELECT * FROM seed_features WHERE quality_feature > #{minQuality} AND deleted_flag = 0 " +
            "ORDER BY quality_feature DESC, user_rating DESC LIMIT #{limit}")
    List<SeedFeatures> selectHighQualityFeatures(@Param("minQuality") Double minQuality, @Param("limit") Integer limit);

    /**
     * 查询价格区间商品特征
     */
    @Select("SELECT * FROM seed_features WHERE price_feature BETWEEN #{minPrice} AND #{maxPrice} " +
            "AND deleted_flag = 0 ORDER BY recommendation_weight DESC LIMIT #{limit}")
    List<SeedFeatures> selectPriceRangeFeatures(@Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("limit") Integer limit);
}
