package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单商品明细Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询商品明细
     */
    @Select("SELECT * FROM order_item WHERE order_id = #{orderId} AND deleted_flag = 0 ORDER BY create_time ASC")
    List<OrderItem> selectByOrderId(Long orderId);
}
