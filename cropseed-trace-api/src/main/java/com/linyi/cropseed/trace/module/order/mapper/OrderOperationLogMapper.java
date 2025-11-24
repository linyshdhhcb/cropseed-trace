package com.linyi.cropseed.trace.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.module.order.model.entity.OrderOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单操作日志Mapper
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface OrderOperationLogMapper extends BaseMapper<OrderOperationLog> {

    /**
     * 根据订单ID查询操作日志
     */
    @Select("SELECT * FROM order_operation_log WHERE order_id = #{orderId} AND deleted_flag = 0 ORDER BY create_time ASC")
    List<OrderOperationLog> selectByOrderId(Long orderId);
}
