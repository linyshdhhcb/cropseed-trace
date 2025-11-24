package com.linyi.cropseed.trace.module.trace.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 溯源记录表
 *
 * @author linyi
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("trace_record")
public class TraceRecord implements Serializable {

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
     * 批次ID
     */
    @TableField("batch_id")
    private Long batchId;

    /**
     * 记录类型：1-生产记录，2-质检记录，3-流通记录，4-销售记录，5-异常记录
     */
    @TableField("record_type")
    private Integer recordType;

    /**
     * 记录阶段
     */
    @TableField("record_stage")
    private String recordStage;

    /**
     * 相关实体ID
     */
    @TableField("entity_id")
    private Long entityId;

    /**
     * 相关实体名称
     */
    @TableField("entity_name")
    private String entityName;

    /**
     * 操作人员
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 操作人电话
     */
    @TableField("operator_phone")
    private String operatorPhone;

    /**
     * 记录时间
     */
    @TableField("record_time")
    private LocalDateTime recordTime;

    /**
     * 位置信息
     */
    @TableField("location")
    private String location;

    /**
     * 温度(℃)
     */
    @TableField("temperature")
    private BigDecimal temperature;

    /**
     * 湿度(%)
     */
    @TableField("humidity")
    private BigDecimal humidity;

    /**
     * 数量
     */
    @TableField("quantity")
    private BigDecimal quantity;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 质量等级
     */
    @TableField("quality_grade")
    private String qualityGrade;

    /**
     * 检测结果
     */
    @TableField("test_result")
    private String testResult;

    /**
     * 内容摘要
     */
    @TableField("content_summary")
    private String contentSummary;

    /**
     * 详细内容(JSON格式)
     */
    @TableField("detailed_content")
    private String detailedContent;

    /**
     * 图片URLs(JSON数组)
     */
    @TableField("image_urls")
    private String imageUrls;

    /**
     * 附件URLs(JSON数组)
     */
    @TableField("attachment_urls")
    private String attachmentUrls;

    /**
     * 区块链交易哈希
     */
    @TableField("blockchain_tx_hash")
    private String blockchainTxHash;

    /**
     * 区块链状态：0-未上链，1-上链中，2-上链成功，3-上链失败
     */
    @TableField("blockchain_status")
    private Integer blockchainStatus;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

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
