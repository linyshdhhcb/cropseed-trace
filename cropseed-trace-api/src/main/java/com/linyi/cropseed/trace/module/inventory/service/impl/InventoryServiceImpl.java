package com.linyi.cropseed.trace.module.inventory.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.IdGenerator;
import com.linyi.cropseed.trace.common.util.SecurityUtils;
import com.linyi.cropseed.trace.entity.*;
import com.linyi.cropseed.trace.mapper.*;
import com.linyi.cropseed.trace.module.inventory.mapper.InboundRecordMapper;
import com.linyi.cropseed.trace.module.inventory.mapper.InventoryMapper;
import com.linyi.cropseed.trace.module.inventory.mapper.WarehouseMapper;
import com.linyi.cropseed.trace.module.inventory.model.entity.InboundRecord;
import com.linyi.cropseed.trace.module.inventory.model.entity.Inventory;
import com.linyi.cropseed.trace.module.seed.mapper.SeedBatchMapper;
import com.linyi.cropseed.trace.module.seed.mapper.SeedInfoMapper;
import com.linyi.cropseed.trace.module.seed.model.entity.SeedBatch;
import com.linyi.cropseed.trace.module.seed.model.entity.SeedInfo;
import com.linyi.cropseed.trace.module.inventory.service.InventoryService;
import com.linyi.cropseed.trace.module.inventory.model.vo.InventoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存服务实现
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    private final SeedInfoMapper seedInfoMapper;
    private final SeedBatchMapper seedBatchMapper;
    private final WarehouseMapper warehouseMapper;
    private final InboundRecordMapper inboundRecordMapper;
    private final OutboundRecordMapper outboundRecordMapper;

    @Override
    public PageResult<InventoryVO> pageInventories(PageQuery pageQuery, Long seedId, Long warehouseId,
            String seedName) {
        Page<Inventory> page = pageQuery.toMpPageWithOrder();

        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(seedId != null, Inventory::getSeedId, seedId)
                .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId)
                .orderByDesc(Inventory::getCreateTime);

        Page<Inventory> inventoryPage = this.page(page, wrapper);

        List<InventoryVO> inventoryVOList = inventoryPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(inventoryPage, inventoryVOList);
    }

    @Override
    public List<InventoryVO> getInventoriesBySeedId(Long seedId) {
        List<Inventory> inventories = baseMapper.selectBySeedId(seedId);
        return inventories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryVO> getInventoriesByWarehouseId(Long warehouseId) {
        List<Inventory> inventories = baseMapper.selectByWarehouseId(warehouseId);
        return inventories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inbound(Long seedId, Long batchId, Long warehouseId, Integer quantity, String remarks) {
        // 验证种子是否存在
        SeedInfo seedInfo = seedInfoMapper.selectById(seedId);
        if (seedInfo == null) {
            throw new BusinessException(ResultCode.SEED_NOT_EXIST);
        }

        // 验证批次是否存在
        SeedBatch seedBatch = seedBatchMapper.selectById(batchId);
        if (seedBatch == null) {
            throw new BusinessException(ResultCode.SEED_BATCH_NOT_EXIST);
        }

        // 验证仓库是否存在
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null) {
            throw new BusinessException(ResultCode.WAREHOUSE_NOT_EXIST);
        }

        // 查询或创建库存记录
        Inventory inventory = baseMapper.selectBySeedIdAndBatchIdAndWarehouseId(seedId, batchId, warehouseId);
        if (inventory == null) {
            inventory = new Inventory();
            inventory.setSeedId(seedId);
            inventory.setBatchId(batchId);
            inventory.setWarehouseId(warehouseId);
            inventory.setQuantity(0);
            inventory.setLockedQuantity(0);
            inventory.setAvailableQuantity(0);
            inventory.setMinStock(0);
            inventory.setMaxStock(0);
            this.save(inventory);
        }

        // 更新库存数量
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        this.updateById(inventory);

        // 创建入库记录
        InboundRecord inboundRecord = new InboundRecord();
        inboundRecord.setInboundNo(IdGenerator.generateInboundNo());
        inboundRecord.setSeedId(seedId);
        inboundRecord.setBatchId(batchId);
        inboundRecord.setWarehouseId(warehouseId);
        inboundRecord.setQuantity(quantity);
        inboundRecord.setInboundTime(LocalDateTime.now());
        inboundRecord.setOperatorId(SecurityUtils.getCurrentUserId());
        inboundRecord.setRemarks(remarks);
        inboundRecordMapper.insert(inboundRecord);

        log.info("入库成功：种子ID={}, 批次ID={}, 仓库ID={}, 数量={}", seedId, batchId, warehouseId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void outbound(Long seedId, Long batchId, Long warehouseId, Integer quantity, String purpose, Long orderId,
            String remarks) {
        // 查询库存记录
        Inventory inventory = baseMapper.selectBySeedIdAndBatchIdAndWarehouseId(seedId, batchId, warehouseId);
        if (inventory == null || inventory.getAvailableQuantity() < quantity) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH);
        }

        // 扣减库存
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        this.updateById(inventory);

        // 创建出库记录
        OutboundRecord outboundRecord = new OutboundRecord();
        outboundRecord.setOutboundNo(IdGenerator.generateOutboundNo());
        outboundRecord.setSeedId(seedId);
        outboundRecord.setBatchId(batchId);
        outboundRecord.setWarehouseId(warehouseId);
        outboundRecord.setQuantity(quantity);
        outboundRecord.setOutboundTime(LocalDateTime.now());
        outboundRecord.setOperatorId(SecurityUtils.getCurrentUserId());
        outboundRecord.setPurpose(purpose);
        outboundRecord.setOrderId(orderId);
        outboundRecord.setRemarks(remarks);
        outboundRecordMapper.insert(outboundRecord);

        log.info("出库成功：种子ID={}, 批次ID={}, 仓库ID={}, 数量={}", seedId, batchId, warehouseId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockInventory(Long seedId, Long batchId, Long warehouseId, Integer quantity) {
        Inventory inventory = baseMapper.selectBySeedIdAndBatchIdAndWarehouseId(seedId, batchId, warehouseId);
        if (inventory == null || inventory.getAvailableQuantity() < quantity) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH);
        }

        int result = baseMapper.lockInventory(inventory.getId(), quantity);
        if (result == 0) {
            throw new BusinessException(ResultCode.INVENTORY_LOCKED);
        }

        log.info("库存锁定成功：种子ID={}, 批次ID={}, 仓库ID={}, 数量={}", seedId, batchId, warehouseId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockInventory(Long seedId, Long batchId, Long warehouseId, Integer quantity) {
        Inventory inventory = baseMapper.selectBySeedIdAndBatchIdAndWarehouseId(seedId, batchId, warehouseId);
        if (inventory == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_EXIST);
        }

        baseMapper.unlockInventory(inventory.getId(), quantity);
        log.info("库存解锁成功：种子ID={}, 批次ID={}, 仓库ID={}, 数量={}", seedId, batchId, warehouseId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductInventory(Long seedId, Long batchId, Long warehouseId, Integer quantity) {
        Inventory inventory = baseMapper.selectBySeedIdAndBatchIdAndWarehouseId(seedId, batchId, warehouseId);
        if (inventory == null || inventory.getLockedQuantity() < quantity) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH);
        }

        int result = baseMapper.deductInventory(inventory.getId(), quantity);
        if (result == 0) {
            throw new BusinessException(ResultCode.INVENTORY_OPERATION_FAIL);
        }

        log.info("库存扣减成功：种子ID={}, 批次ID={}, 仓库ID={}, 数量={}", seedId, batchId, warehouseId, quantity);
    }

    @Override
    public InventoryVO getInventoryDetail(Long seedId, Long batchId, Long warehouseId) {
        Inventory inventory = baseMapper.selectBySeedIdAndBatchIdAndWarehouseId(seedId, batchId, warehouseId);
        if (inventory == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_EXIST);
        }
        return convertToVO(inventory);
    }

    @Override
    public List<InventoryVO> getLowStockInventories() {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("quantity <= min_stock AND min_stock > 0");
        List<Inventory> inventories = this.list(wrapper);
        return inventories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStockThreshold(Long id, Integer minStock, Integer maxStock) {
        Inventory inventory = this.getById(id);
        if (inventory == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_EXIST);
        }

        inventory.setMinStock(minStock);
        inventory.setMaxStock(maxStock);
        this.updateById(inventory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustInventory(Long inventoryId, String adjustType, Integer adjustQuantity, String reason) {
        Inventory inventory = this.getById(inventoryId);
        if (inventory == null) {
            throw new BusinessException(ResultCode.INVENTORY_NOT_EXIST);
        }
        if (adjustQuantity == null || adjustQuantity <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if ("decrease".equalsIgnoreCase(adjustType)) {
            if (inventory.getAvailableQuantity() < adjustQuantity) {
                throw new BusinessException(ResultCode.INVENTORY_NOT_ENOUGH);
            }
            inventory.setQuantity(inventory.getQuantity() - adjustQuantity);
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - adjustQuantity);
        } else { // increase 默认
            inventory.setQuantity(inventory.getQuantity() + adjustQuantity);
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + adjustQuantity);
        }
        this.updateById(inventory);

        log.info("库存调整：inventoryId={}, type={}, qty={}, reason={}", inventoryId, adjustType, adjustQuantity, reason);
    }

    /**
     * 转换为VO对象
     */
    private InventoryVO convertToVO(Inventory inventory) {
        InventoryVO inventoryVO = BeanUtil.copyProperties(inventory, InventoryVO.class);

        // 查询种子信息
        SeedInfo seedInfo = seedInfoMapper.selectById(inventory.getSeedId());
        if (seedInfo != null) {
            inventoryVO.setSeedName(seedInfo.getSeedName());
            inventoryVO.setSeedCode(seedInfo.getSeedCode());
        }

        // 查询批次信息
        SeedBatch seedBatch = seedBatchMapper.selectById(inventory.getBatchId());
        if (seedBatch != null) {
            inventoryVO.setBatchNo(seedBatch.getBatchNo());
        }

        // 查询仓库信息
        Warehouse warehouse = warehouseMapper.selectById(inventory.getWarehouseId());
        if (warehouse != null) {
            inventoryVO.setWarehouseName(warehouse.getWarehouseName());
            inventoryVO.setWarehouseCode(warehouse.getWarehouseCode());
        }

        return inventoryVO;
    }

    @Override
    public List<Inventory> getAvailableInventoryBySeedId(Long seedId) {
        return baseMapper.selectAvailableInventoryBySeedId(seedId);
    }

    @Override
    public Integer getTotalInventoryBySeedId(Long seedId) {
        return baseMapper.selectTotalInventoryBySeedId(seedId);
    }
}
