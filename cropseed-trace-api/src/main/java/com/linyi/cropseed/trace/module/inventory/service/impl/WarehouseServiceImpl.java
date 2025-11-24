package com.linyi.cropseed.trace.module.inventory.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.module.inventory.model.entity.Inventory;
import com.linyi.cropseed.trace.entity.Warehouse;
import com.linyi.cropseed.trace.module.inventory.mapper.InventoryMapper;
import com.linyi.cropseed.trace.module.inventory.mapper.WarehouseMapper;
import com.linyi.cropseed.trace.module.inventory.service.WarehouseService;
import com.linyi.cropseed.trace.module.inventory.model.vo.WarehouseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仓库服务实现
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    private final InventoryMapper inventoryMapper;

    @Override
    public PageResult<WarehouseVO> pageWarehouses(PageQuery pageQuery, String warehouseName, String warehouseCode,
            String manager, Integer status) {
        Page<Warehouse> page = pageQuery.toMpPageWithOrder();

        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(warehouseName != null, Warehouse::getWarehouseName, warehouseName)
                .like(warehouseCode != null, Warehouse::getWarehouseCode, warehouseCode)
                .like(manager != null, Warehouse::getManager, manager)
                .eq(status != null, Warehouse::getStatus, status)
                .orderByDesc(Warehouse::getCreateTime);

        Page<Warehouse> warehousePage = this.page(page, wrapper);

        List<WarehouseVO> warehouseVOList = warehousePage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(warehousePage, warehouseVOList);
    }

    @Override
    public List<WarehouseVO> getAllActiveWarehouses() {
        List<Warehouse> warehouses = baseMapper.selectAllActiveWarehouses();
        return warehouses.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseVO getWarehouseById(Long id) {
        Warehouse warehouse = this.getById(id);
        if (warehouse == null) {
            throw new BusinessException(ResultCode.WAREHOUSE_NOT_EXIST);
        }
        return convertToVO(warehouse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addWarehouse(Warehouse warehouse) {
        // 检查仓库编码是否重复
        if (getWarehouseByCode(warehouse.getWarehouseCode()) != null) {
            throw new BusinessException(ResultCode.WAREHOUSE_CODE_DUPLICATE);
        }

        // 设置默认值
        if (warehouse.getUsedCapacity() == null) {
            warehouse.setUsedCapacity(BigDecimal.ZERO);
        }

        this.save(warehouse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarehouse(Warehouse warehouse) {
        Warehouse existWarehouse = this.getById(warehouse.getId());
        if (existWarehouse == null) {
            throw new BusinessException(ResultCode.WAREHOUSE_NOT_EXIST);
        }

        // 检查仓库编码是否被其他仓库使用
        Warehouse warehouseByCode = getWarehouseByCode(warehouse.getWarehouseCode());
        if (warehouseByCode != null && !warehouseByCode.getId().equals(warehouse.getId())) {
            throw new BusinessException(ResultCode.WAREHOUSE_CODE_DUPLICATE);
        }

        this.updateById(warehouse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = this.getById(id);
        if (warehouse == null) {
            throw new BusinessException(ResultCode.WAREHOUSE_NOT_EXIST);
        }

        // 检查是否有库存记录，如果有则不允许删除
        LambdaQueryWrapper<Inventory> inventoryWrapper = new LambdaQueryWrapper<>();
        inventoryWrapper.eq(Inventory::getWarehouseId, id);
        Long inventoryCount = inventoryMapper.selectCount(inventoryWrapper);
        if (inventoryCount > 0) {
            throw new BusinessException(ResultCode.WAREHOUSE_HAS_INVENTORY);
        }

        this.removeById(id);
    }

    @Override
    public Warehouse getWarehouseByCode(String warehouseCode) {
        return baseMapper.selectByWarehouseCode(warehouseCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarehouseCapacity(Long warehouseId, BigDecimal usedCapacity) {
        Warehouse warehouse = this.getById(warehouseId);
        if (warehouse == null) {
            throw new BusinessException(ResultCode.WAREHOUSE_NOT_EXIST);
        }

        warehouse.setUsedCapacity(usedCapacity);
        this.updateById(warehouse);
    }

    /**
     * 转换为VO对象
     */
    private WarehouseVO convertToVO(Warehouse warehouse) {
        WarehouseVO warehouseVO = BeanUtil.copyProperties(warehouse, WarehouseVO.class);

        // 计算容量使用率
        if (warehouse.getCapacity() != null && warehouse.getCapacity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal usageRate = warehouse.getUsedCapacity()
                    .divide(warehouse.getCapacity(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            warehouseVO.setUsageRate(usageRate);
        }

        return warehouseVO;
    }

    @Override
    @Transactional
    public void batchDeleteWarehouses(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        removeByIds(ids);
    }
}
