package com.linyi.cropseed.trace.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.module.inventory.model.entity.InventoryOutbound;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库记录Mapper
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface InventoryOutboundMapper extends BaseMapper<InventoryOutbound> {
}
