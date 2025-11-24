package com.linyi.cropseed.trace.module.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.entity.Warehouse;
import com.linyi.cropseed.trace.module.inventory.model.vo.WarehouseVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 仓库服务接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface WarehouseService extends IService<Warehouse> {

    /**
     * 分页查询仓库列表
     */
    PageResult<WarehouseVO> pageWarehouses(PageQuery pageQuery, String warehouseName, String warehouseCode,
            String manager, Integer status);

    /**
     * 查询所有启用的仓库
     */
    List<WarehouseVO> getAllActiveWarehouses();

    /**
     * 根据ID查询仓库详情
     */
    WarehouseVO getWarehouseById(Long id);

    /**
     * 新增仓库
     */
    void addWarehouse(Warehouse warehouse);

    /**
     * 修改仓库
     */
    void updateWarehouse(Warehouse warehouse);

    /**
     * 删除仓库
     */
    void deleteWarehouse(Long id);

    /**
     * 根据仓库编码查询仓库
     */
    Warehouse getWarehouseByCode(String warehouseCode);

    /**
     * 更新仓库容量使用情况
     */
    void updateWarehouseCapacity(Long warehouseId, BigDecimal usedCapacity);

    /**
     * 批量删除仓库
     */
    void batchDeleteWarehouses(List<Long> ids);
}
