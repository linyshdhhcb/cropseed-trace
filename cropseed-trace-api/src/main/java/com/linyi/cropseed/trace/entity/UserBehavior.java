package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.linyi.cropseed.trace.infrastructure.persistence.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户行为记录实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_behavior")
public class UserBehavior extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 行为类型：1-浏览，2-搜索，3-收藏，4-加购物车，5-购买，6-评价
     */
    @TableField("behavior_type")
    private Integer behaviorType;

    /**
     * 目标类型：1-种子，2-品类，3-品牌
     */
    @TableField("target_type")
    private Integer targetType;

    /**
     * 目标ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 行为内容
     */
    @TableField("behavior_content")
    private String behaviorContent;

    /**
     * 行为权重：0-1之间的小数
     */
    @TableField("behavior_weight")
    private Double behaviorWeight;

    /**
     * 行为时间
     */
    @TableField("behavior_time")
    private LocalDateTime behaviorTime;

    /**
     * 会话ID
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 设备信息
     */
    @TableField("device_info")
    private String deviceInfo;

    /**
     * IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 来源页面
     */
    @TableField("referrer")
    private String referrer;

    /**
     * 停留时长（秒）
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 行为评分：1-5分
     */
    @TableField("rating")
    private Integer rating;
}
