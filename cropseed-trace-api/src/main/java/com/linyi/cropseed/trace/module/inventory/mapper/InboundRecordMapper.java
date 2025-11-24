package com.linyi.cropseed.trace.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.module.inventory.model.entity.InboundRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 入库记录Mapper
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface InboundRecordMapper extends BaseMapper<InboundRecord> {

    /**
     * 根据入库单号查询记录
     */
    @Select("SELECT * FROM inbound_record WHERE inbound_no = #{inboundNo} AND deleted_flag = 0")
    InboundRecord selectByInboundNo(String inboundNo);

    /**
     * 根据种子ID查询入库记录
     */
    @Select("SELECT * FROM inbound_record WHERE seed_id = #{seedId} AND deleted_flag = 0 ORDER BY inbound_time DESC")
    List<InboundRecord> selectBySeedId(Long seedId);

    /**
     * 根据仓库ID查询入库记录
     */
    @Select("SELECT * FROM inbound_record WHERE warehouse_id = #{warehouseId} AND deleted_flag = 0 ORDER BY inbound_time DESC")
    List<InboundRecord> selectByWarehouseId(Long warehouseId);

}
