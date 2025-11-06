package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单信息Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM order_info WHERE order_no = #{orderNo} AND deleted_flag = 0")
    OrderInfo selectByOrderNo(String orderNo);

    /**
     * 根据用户ID查询订单列表
     */
    @Select("SELECT * FROM order_info WHERE user_id = #{userId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<OrderInfo> selectByUserId(Long userId);

    /**
     * 根据订单状态查询订单列表
     */
    @Select("SELECT * FROM order_info WHERE order_status = #{orderStatus} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<OrderInfo> selectByOrderStatus(Integer orderStatus);
}
