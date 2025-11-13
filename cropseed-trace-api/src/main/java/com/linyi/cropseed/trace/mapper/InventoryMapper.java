package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 库存台账Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 根据种子ID查询库存
     */
    @Select("SELECT * FROM inventory WHERE seed_id = #{seedId} AND deleted_flag = 0")
    List<Inventory> selectBySeedId(Long seedId);

    /**
     * 根据仓库ID查询库存
     */
    @Select("SELECT * FROM inventory WHERE warehouse_id = #{warehouseId} AND deleted_flag = 0")
    List<Inventory> selectByWarehouseId(Long warehouseId);

    /**
     * 根据种子ID和仓库ID查询库存
     */
    @Select("SELECT * FROM inventory WHERE seed_id = #{seedId} AND warehouse_id = #{warehouseId} AND deleted_flag = 0")
    Inventory selectBySeedIdAndWarehouseId(Long seedId, Long warehouseId);

    /**
     * 根据种子ID、批次ID和仓库ID查询库存
     */
    @Select("SELECT * FROM inventory WHERE seed_id = #{seedId} AND batch_id = #{batchId} AND warehouse_id = #{warehouseId} AND deleted_flag = 0")
    Inventory selectBySeedIdAndBatchIdAndWarehouseId(Long seedId, Long batchId, Long warehouseId);

    /**
     * 更新库存数量
     */
    @Update("UPDATE inventory SET quantity = quantity + #{quantity}, available_quantity = available_quantity + #{quantity} WHERE id = #{id}")
    int updateQuantity(Long id, Integer quantity);

    /**
     * 锁定库存
     */
    @Update("UPDATE inventory SET locked_quantity = locked_quantity + #{quantity}, available_quantity = available_quantity - #{quantity} WHERE id = #{id} AND available_quantity >= #{quantity}")
    int lockInventory(Long id, Integer quantity);

    /**
     * 释放锁定库存
     */
    @Update("UPDATE inventory SET locked_quantity = locked_quantity - #{quantity}, available_quantity = available_quantity + #{quantity} WHERE id = #{id}")
    int unlockInventory(Long id, Integer quantity);

    /**
     * 扣减库存
     */
    @Update("UPDATE inventory SET quantity = quantity - #{quantity}, locked_quantity = locked_quantity - #{quantity} WHERE id = #{id} AND locked_quantity >= #{quantity}")
    int deductInventory(Long id, Integer quantity);

    /**
     * 根据种子ID获取可用库存（按生产日期排序，先进先出）
     */
    @Select("SELECT i.* FROM inventory i " +
            "LEFT JOIN seed_batch sb ON i.batch_id = sb.id " +
            "WHERE i.seed_id = #{seedId} AND i.available_quantity > 0 AND i.deleted_flag = 0 " +
            "ORDER BY sb.production_date ASC")
    List<Inventory> selectAvailableInventoryBySeedId(Long seedId);

    /**
     * 获取种子总库存数量
     */
    @Select("SELECT COALESCE(SUM(available_quantity), 0) FROM inventory WHERE seed_id = #{seedId} AND deleted_flag = 0")
    Integer selectTotalInventoryBySeedId(Long seedId);
}
