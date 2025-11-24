package com.linyi.cropseed.trace.module.recommendation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.module.recommendation.model.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户行为Mapper
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

        /**
         * 根据用户ID查询行为记录
         */
        @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} AND deleted_flag = 0 ORDER BY behavior_time DESC LIMIT #{limit}")
        List<UserBehavior> selectByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

        /**
         * 根据用户ID和行为类型查询行为记录
         */
        @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} AND behavior_type = #{behaviorType} AND deleted_flag = 0 ORDER BY behavior_time DESC")
        List<UserBehavior> selectByUserIdAndType(@Param("userId") Long userId,
                        @Param("behaviorType") Integer behaviorType);

        /**
         * 查询用户对特定目标的评分
         */
        @Select("SELECT * FROM user_behavior WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} AND behavior_type = 6 AND deleted_flag = 0")
        UserBehavior selectUserRating(@Param("userId") Long userId,
                        @Param("targetType") Integer targetType,
                        @Param("targetId") Long targetId);

        /**
         * 查询相似用户的行为记录
         */
        @Select("SELECT ub.* FROM user_behavior ub " +
                        "INNER JOIN user_profile up ON ub.user_id = up.user_id " +
                        "WHERE up.user_type = #{userType} AND up.region = #{region} " +
                        "AND ub.behavior_type = #{behaviorType} AND ub.deleted_flag = 0 " +
                        "ORDER BY ub.behavior_time DESC LIMIT #{limit}")
        List<UserBehavior> selectSimilarUserBehaviors(@Param("userType") Integer userType,
                        @Param("region") String region,
                        @Param("behaviorType") Integer behaviorType,
                        @Param("limit") Integer limit);

        /**
         * 查询热门行为记录
         */
        @Select("SELECT * FROM user_behavior WHERE behavior_type = #{behaviorType} " +
                        "AND behavior_time >= #{startTime} AND deleted_flag = 0 " +
                        "ORDER BY behavior_weight DESC LIMIT #{limit}")
        List<UserBehavior> selectPopularBehaviors(@Param("behaviorType") Integer behaviorType,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("limit") Integer limit);

        /**
         * 统计用户行为次数
         */
        @Select("SELECT COUNT(*) FROM user_behavior WHERE user_id = #{userId} AND behavior_type = #{behaviorType} AND deleted_flag = 0")
        Integer countUserBehaviors(@Param("userId") Long userId, @Param("behaviorType") Integer behaviorType);

        /**
         * 根据目标ID和行为类型查询行为记录（用于计算商品特征）
         */
        @Select("SELECT * FROM user_behavior WHERE target_id = #{targetId} AND target_type = #{targetType} AND behavior_type = #{behaviorType} AND deleted_flag = 0 ORDER BY behavior_time DESC")
        List<UserBehavior> selectByTargetIdAndType(@Param("targetId") Long targetId,
                        @Param("targetType") Integer targetType, @Param("behaviorType") Integer behaviorType);

        /**
         * 根据目标ID查询所有行为记录（用于计算市场热度）
         */
        @Select("SELECT * FROM user_behavior WHERE target_id = #{targetId} AND target_type = #{targetType} AND deleted_flag = 0 ORDER BY behavior_time DESC LIMIT #{limit}")
        List<UserBehavior> selectByTargetId(@Param("targetId") Long targetId, @Param("targetType") Integer targetType,
                        @Param("limit") Integer limit);

        /**
         * 批量查询多个用户的行为记录（优化N+1查询问题）
         */
        @Select({
                "<script>",
                "SELECT * FROM user_behavior",
                "WHERE user_id IN",
                "<foreach collection='userIds' item='userId' open='(' separator=',' close=')'>",
                "#{userId}",
                "</foreach>",
                "AND deleted_flag = 0",
                "ORDER BY behavior_time DESC",
                "LIMIT #{limit}",
                "</script>"
        })
        List<UserBehavior> selectByUserIds(@Param("userIds") List<Long> userIds, @Param("limit") Integer limit);
}
