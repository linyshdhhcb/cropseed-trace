package com.linyi.cropseed.trace.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.entity.Inventory;
import com.linyi.cropseed.trace.vo.InventoryVO;

import java.util.List;

/**
 * 库存服务接口
 * 
 * @author LinYi
 * @since 2025-10-25
 */
public interface InventoryService extends IService<Inventory> {

    /**
     * 分页查询库存列表
     */
    PageResult<InventoryVO> pageInventories(PageQuery pageQuery, Long seedId, Long warehouseId, String seedName);

    /**
     * 根据种子ID查询库存
     */
    List<InventoryVO> getInventoriesBySeedId(Long seedId);

    /**
     * 根据仓库ID查询库存
     */
    List<InventoryVO> getInventoriesByWarehouseId(Long warehouseId);

    /**
     * 入库操作
     */
    void inbound(Long seedId, Long batchId, Long warehouseId, Integer quantity, String remarks);

    /**
     * 出库操作
     */
    void outbound(Long seedId, Long batchId, Long warehouseId, Integer quantity, String purpose, Long orderId,
            String remarks);

    /**
     * 锁定库存
     */
    void lockInventory(Long seedId, Long batchId, Long warehouseId, Integer quantity);

    /**
     * 释放锁定库存
     */
    void unlockInventory(Long seedId, Long batchId, Long warehouseId, Integer quantity);

    /**
     * 扣减库存
     */
    void deductInventory(Long seedId, Long batchId, Long warehouseId, Integer quantity);

    /**
     * 获取库存详情
     */
    InventoryVO getInventoryDetail(Long seedId, Long batchId, Long warehouseId);

    /**
     * 库存预警检查
     */
    List<InventoryVO> getLowStockInventories();

    /**
     * 更新库存预警阈值
     */
    void updateStockThreshold(Long id, Integer minStock, Integer maxStock);

    /**
     * 手工调整库存（正负增减可用与总量）
     */
    void adjustInventory(Long inventoryId, String adjustType, Integer adjustQuantity, String reason);
}
