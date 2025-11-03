package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.entity.InventoryInbound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库记录Mapper
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface InventoryInboundMapper extends BaseMapper<InventoryInbound> {
    Page<InventoryInbound> queryPage(Page<InventoryInbound> page, String batchNo, Long warehouseId, Integer status);
}
