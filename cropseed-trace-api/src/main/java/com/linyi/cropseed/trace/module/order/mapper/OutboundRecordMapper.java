package com.linyi.cropseed.trace.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.module.order.model.entity.OutboundRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 出库记录Mapper
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface OutboundRecordMapper extends BaseMapper<OutboundRecord> {

    /**
     * 根据出库单号查询记录
     */
    @Select("SELECT * FROM outbound_record WHERE outbound_no = #{outboundNo} AND deleted_flag = 0")
    OutboundRecord selectByOutboundNo(String outboundNo);

    /**
     * 根据种子ID查询出库记录
     */
    @Select("SELECT * FROM outbound_record WHERE seed_id = #{seedId} AND deleted_flag = 0 ORDER BY outbound_time DESC")
    List<OutboundRecord> selectBySeedId(Long seedId);

    /**
     * 根据仓库ID查询出库记录
     */
    @Select("SELECT * FROM outbound_record WHERE warehouse_id = #{warehouseId} AND deleted_flag = 0 ORDER BY outbound_time DESC")
    List<OutboundRecord> selectByWarehouseId(Long warehouseId);

    /**
     * 根据订单ID查询出库记录
     */
    @Select("SELECT * FROM outbound_record WHERE order_id = #{orderId} AND deleted_flag = 0 ORDER BY outbound_time DESC")
    List<OutboundRecord> selectByOrderId(Long orderId);
}
