package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.entity.Warehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 仓库信息Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface WarehouseMapper extends BaseMapper<Warehouse> {

    /**
     * 根据仓库编码查询仓库
     */
    @Select("SELECT * FROM warehouse WHERE warehouse_code = #{warehouseCode} AND deleted_flag = 0")
    Warehouse selectByWarehouseCode(String warehouseCode);

    /**
     * 查询所有启用的仓库
     */
    @Select("SELECT * FROM warehouse WHERE deleted_flag = 0 AND status = 1 ORDER BY create_time ASC")
    List<Warehouse> selectAllActiveWarehouses();
}
