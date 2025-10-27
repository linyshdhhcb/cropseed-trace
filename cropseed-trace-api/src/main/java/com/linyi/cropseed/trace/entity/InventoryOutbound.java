package com.linyi.cropseed.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 出库记录实体
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("inventory_outbound")
public class InventoryOutbound {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String batchNo;
    private Long warehouseId;
    private Long seedBatchId;
    private Integer quantity;
    private String recipient;
    private String purpose;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
