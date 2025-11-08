package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.entity.Recommendation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 推荐结果Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface RecommendationMapper extends BaseMapper<Recommendation> {

    /**
     * 根据用户ID查询推荐结果
     */
    @Select("SELECT * FROM recommendation WHERE user_id = #{userId} AND deleted_flag = 0 " +
            "ORDER BY recommendation_score DESC LIMIT #{limit}")
    List<Recommendation> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 根据用户ID和推荐类型查询推荐结果
     */
    @Select("SELECT * FROM recommendation WHERE user_id = #{userId} AND recommendation_type = #{recommendationType} " +
            "AND deleted_flag = 0 ORDER BY recommendation_score DESC LIMIT #{limit}")
    List<Recommendation> selectByUserIdAndType(@Param("userId") Long userId,
            @Param("recommendationType") Integer recommendationType,
            @Param("limit") Integer limit);

    /**
     * 查询未展示的推荐结果
     */
    @Select("SELECT * FROM recommendation WHERE user_id = #{userId} AND is_shown = 0 AND deleted_flag = 0 " +
            "ORDER BY recommendation_score DESC LIMIT #{limit}")
    List<Recommendation> selectUnshownRecommendations(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 查询热门推荐结果
     */
    @Select("SELECT * FROM recommendation WHERE recommendation_type = #{recommendationType} " +
            "AND deleted_flag = 0 ORDER BY recommendation_score DESC LIMIT #{limit}")
    List<Recommendation> selectPopularRecommendations(@Param("recommendationType") Integer recommendationType,
            @Param("limit") Integer limit);

    /**
     * 统计推荐效果
     */
    @Select("SELECT COUNT(*) FROM recommendation WHERE user_id = #{userId} AND is_clicked = 1 AND deleted_flag = 0")
    Integer countClickedRecommendations(Long userId);

    @Select("SELECT COUNT(*) FROM recommendation WHERE user_id = #{userId} AND is_purchased = 1 AND deleted_flag = 0")
    Integer countPurchasedRecommendations(Long userId);

    /**
     * 查询推荐批次结果
     */
    @Select("SELECT * FROM recommendation WHERE batch_id = #{batchId} AND deleted_flag = 0 " +
            "ORDER BY recommendation_score DESC")
    List<Recommendation> selectByBatchId(String batchId);
}
