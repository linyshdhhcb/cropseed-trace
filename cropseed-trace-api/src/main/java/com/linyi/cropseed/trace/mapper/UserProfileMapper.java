package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户画像Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    /**
     * 根据用户ID查询画像
     */
    @Select("SELECT * FROM user_profile WHERE user_id = #{userId} AND deleted_flag = 0")
    UserProfile selectByUserId(Long userId);

    /**
     * 查询相似用户画像
     */
    @Select("SELECT * FROM user_profile WHERE user_type = #{userType} AND region = #{region} AND deleted_flag = 0 ORDER BY recommendation_weight DESC LIMIT #{limit}")
    List<UserProfile> selectSimilarProfiles(@Param("userType") Integer userType,
            @Param("region") String region,
            @Param("limit") Integer limit);

    /**
     * 查询活跃用户画像
     */
    @Select("SELECT * FROM user_profile WHERE activity_level > #{minActivity} AND deleted_flag = 0 ORDER BY activity_level DESC LIMIT #{limit}")
    List<UserProfile> selectActiveProfiles(@Param("minActivity") Double minActivity,
            @Param("limit") Integer limit);
}
