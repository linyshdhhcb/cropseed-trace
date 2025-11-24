package com.linyi.cropseed.trace.module.trace.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消费者溯源查询记录表
 *
 * @author linyi
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("trace_query")
public class TraceQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 溯源码
     */
    @TableField("trace_code")
    private String traceCode;

    /**
     * 查询用户ID(如果已登录)
     */
    @TableField("query_user_id")
    private Long queryUserId;

    /**
     * 微信用户openid
     */
    @TableField("query_openid")
    private String queryOpenid;

    /**
     * 查询IP地址
     */
    @TableField("query_ip")
    private String queryIp;

    /**
     * 查询地理位置
     */
    @TableField("query_location")
    private String queryLocation;

    /**
     * 查询设备信息
     */
    @TableField("query_device")
    private String queryDevice;

    /**
     * 查询渠道：1-小程序扫码，2-小程序输入，3-网页查询，4-APP查询
     */
    @TableField("query_channel")
    private Integer queryChannel;

    /**
     * 查询时间
     */
    @TableField("query_time")
    private LocalDateTime queryTime;

    /**
     * 查询结果：1-成功，2-溯源码不存在，3-溯源码已过期，4-系统错误
     */
    @TableField("query_result")
    private Integer queryResult;

    /**
     * 响应时间(毫秒)
     */
    @TableField("response_time")
    private Integer responseTime;

    /**
     * 用户反馈
     */
    @TableField("user_feedback")
    private String userFeedback;

    /**
     * 满意度评分：1-5分
     */
    @TableField("satisfaction_score")
    private Integer satisfactionScore;

    /**
     * 反馈时间
     */
    @TableField("feedback_time")
    private LocalDateTime feedbackTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建用户ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 修改用户ID
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 删除标记
     */
    @TableField("deleted_flag")
    @TableLogic
    private Integer deletedFlag;
}
