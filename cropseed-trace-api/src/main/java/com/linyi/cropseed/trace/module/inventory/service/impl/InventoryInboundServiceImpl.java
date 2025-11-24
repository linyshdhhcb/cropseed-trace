package com.linyi.cropseed.trace.module.inventory.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.module.inventory.model.dto.InventoryInboundDTO;
import com.linyi.cropseed.trace.module.inventory.model.entity.Inventory;
import com.linyi.cropseed.trace.module.inventory.model.entity.InventoryInbound;
import com.linyi.cropseed.trace.module.seed.model.entity.SeedBatch;
import com.linyi.cropseed.trace.module.inventory.model.entity.Warehouse;
import com.linyi.cropseed.trace.module.inventory.mapper.InventoryInboundMapper;
import com.linyi.cropseed.trace.module.inventory.mapper.InventoryMapper;
import com.linyi.cropseed.trace.module.seed.mapper.SeedBatchMapper;
import com.linyi.cropseed.trace.module.inventory.mapper.WarehouseMapper;
import com.linyi.cropseed.trace.module.inventory.service.InventoryInboundService;
import com.linyi.cropseed.trace.module.inventory.model.vo.InventoryInboundVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 入库管理Service实现类
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryInboundServiceImpl implements InventoryInboundService {

    private final InventoryInboundMapper inventoryInboundMapper;
    private final WarehouseMapper warehouseMapper;
    private final SeedBatchMapper seedBatchMapper;
    private final InventoryMapper inventoryMapper;

    @Override
    public PageResult<InventoryInboundVO> pageInboundRecords(PageQuery pageQuery, String batchNo, Long warehouseId,
            Integer status) {
        Page<InventoryInbound> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        Page<InventoryInbound> result = inventoryInboundMapper.queryPage(page, batchNo, warehouseId, status);

        // LambdaQueryWrapper<InventoryInbound> queryWrapper = new
        // LambdaQueryWrapper<>();
        // queryWrapper.like(StringUtils.hasText(batchNo), InventoryInbound::getBatchNo,
        // batchNo)
        // .eq(warehouseId != null, InventoryInbound::getWarehouseId, warehouseId)
        // .eq(status != null, InventoryInbound::getStatus, status)
        // .orderByDesc(InventoryInbound::getCreateTime);
        //
        // Page<InventoryInbound> result = inventoryInboundMapper.selectPage(page,
        // queryWrapper);

        List<InventoryInboundVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result, voList);
    }

    @Override
    public InventoryInboundVO getInboundRecordById(Long id) {
        InventoryInbound record = inventoryInboundMapper.selectById(id);
        return record != null ? convertToVO(record) : null;
    }

    @Override
    @Transactional
    public void addInboundRecord(InventoryInboundDTO inboundDTO) {
        InventoryInbound record = new InventoryInbound();
        BeanUtils.copyProperties(inboundDTO, record);
        // 设置默认状态为待审核
        record.setStatus(0);
        inventoryInboundMapper.insert(record);
        log.info("新增入库记录成功: {}", record.getBatchNo());
    }

    @Override
    @Transactional
    public void updateInboundRecord(InventoryInboundDTO inboundDTO) {
        InventoryInbound record = new InventoryInbound();
        BeanUtils.copyProperties(inboundDTO, record);
        inventoryInboundMapper.updateById(record);
        log.info("修改入库记录成功: {}", record.getBatchNo());
    }

    @Override
    @Transactional
    public void deleteInboundRecord(Long id) {
        inventoryInboundMapper.deleteById(id);
        log.info("删除入库记录成功: {}", id);
    }

    @Override
    @Transactional
    public void batchDeleteInboundRecords(List<Long> ids) {
        inventoryInboundMapper.deleteBatchIds(ids);
        log.info("批量删除入库记录成功: {}", ids);
    }

    @Override
    @Transactional
    public void approve(Long id, String approveRemark) {
        InventoryInbound record = inventoryInboundMapper.selectById(id);
        if (record == null) {
            return;
        }
        record.setStatus(1); // 审批通过
        record.setRemark(approveRemark);
        inventoryInboundMapper.updateById(record);
    }

    @Override
    @Transactional
    public void confirm(Long id, String confirmRemark) {
        InventoryInbound record = inventoryInboundMapper.selectById(id);
        if (record == null) {
            log.warn("入库记录不存在，ID: {}", id);
            return;
        }

        // 检查状态，只有已审核状态的才能确认入库
        if (record.getStatus() != 1) {
            log.warn("入库记录状态不正确，无法确认入库。ID: {}, 当前状态: {}", id, record.getStatus());
            throw new RuntimeException("只有已审核状态的入库单才能确认入库");
        }

        // 获取种子批次信息
        SeedBatch seedBatch = seedBatchMapper.selectById(record.getSeedBatchId());
        if (seedBatch == null || seedBatch.getSeedId() == null) {
            log.error("种子批次不存在或缺少种子信息，批次ID: {}", record.getSeedBatchId());
            throw new RuntimeException("种子批次不存在或缺少种子信息");
        }

        Long seedId = seedBatch.getSeedId();
        Long batchId = record.getSeedBatchId();
        Long warehouseId = record.getWarehouseId();
        Integer quantity = record.getQuantity();

        // 查询或创建库存记录
        Inventory inventory = inventoryMapper.selectBySeedIdAndBatchIdAndWarehouseId(seedId, batchId, warehouseId);
        if (inventory == null) {
            // 创建新的库存记录
            inventory = new Inventory();
            inventory.setSeedId(seedId);
            inventory.setBatchId(batchId);
            inventory.setWarehouseId(warehouseId);
            inventory.setQuantity(0);
            inventory.setLockedQuantity(0);
            inventory.setAvailableQuantity(0);
            inventory.setMinStock(0);
            inventory.setMaxStock(0);
            inventoryMapper.insert(inventory);
        }

        // 更新库存数量
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventoryMapper.updateById(inventory);

        // 更新入库记录状态为已入库
        record.setStatus(2); // 已入库（状态码：0-待审核，1-已审核，2-已入库）
        record.setRemark(confirmRemark);
        inventoryInboundMapper.updateById(record);

        log.info("确认入库成功：入库记录ID={}, 种子ID={}, 批次ID={}, 仓库ID={}, 数量={}",
                id, seedId, batchId, warehouseId, quantity);
    }

    @Override
    @Transactional
    public void cancel(Long id, String cancelReason) {
        InventoryInbound record = inventoryInboundMapper.selectById(id);
        if (record == null) {
            return;
        }
        record.setStatus(0); // 取消/草稿
        record.setRemark(cancelReason);
        inventoryInboundMapper.updateById(record);
    }

    private InventoryInboundVO convertToVO(InventoryInbound record) {
        InventoryInboundVO vo = new InventoryInboundVO();
        BeanUtils.copyProperties(record, vo);

        // 填充仓库名称
        if (record.getWarehouseId() != null) {
            Warehouse warehouse = warehouseMapper.selectById(record.getWarehouseId());
            if (warehouse != null) {
                vo.setWarehouseName(warehouse.getWarehouseName());
            }
        }

        // 填充种子批次号
        if (record.getSeedBatchId() != null) {
            SeedBatch seedBatch = seedBatchMapper.selectById(record.getSeedBatchId());
            if (seedBatch != null) {
                vo.setSeedBatchNo(seedBatch.getBatchNo());
            }
        }

        return vo;
    }
}
