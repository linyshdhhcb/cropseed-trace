package com.linyi.cropseed.trace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.dto.InventoryOutboundDTO;
import com.linyi.cropseed.trace.entity.Inventory;
import com.linyi.cropseed.trace.entity.InventoryOutbound;
import com.linyi.cropseed.trace.entity.SeedBatch;
import com.linyi.cropseed.trace.entity.Warehouse;
import com.linyi.cropseed.trace.mapper.InventoryMapper;
import com.linyi.cropseed.trace.mapper.InventoryOutboundMapper;
import com.linyi.cropseed.trace.mapper.SeedBatchMapper;
import com.linyi.cropseed.trace.mapper.WarehouseMapper;
import com.linyi.cropseed.trace.service.InventoryOutboundService;
import com.linyi.cropseed.trace.vo.InventoryOutboundVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 出库管理Service实现类
 *
 * @author LinYi
 * @since 2025-10-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryOutboundServiceImpl implements InventoryOutboundService {

    private final InventoryOutboundMapper inventoryOutboundMapper;
    private final InventoryMapper inventoryMapper;
    private final SeedBatchMapper seedBatchMapper;
    private final WarehouseMapper warehouseMapper;

    @Override
    public PageResult<InventoryOutboundVO> pageOutboundRecords(PageQuery pageQuery, String batchNo, Long warehouseId,
            Integer status) {
        Page<InventoryOutbound> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        LambdaQueryWrapper<InventoryOutbound> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(batchNo), InventoryOutbound::getBatchNo, batchNo)
                .eq(warehouseId != null, InventoryOutbound::getWarehouseId, warehouseId)
                .eq(status != null, InventoryOutbound::getStatus, status)
                .orderByDesc(InventoryOutbound::getCreateTime);

        Page<InventoryOutbound> result = inventoryOutboundMapper.selectPage(page, queryWrapper);

        List<InventoryOutboundVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result, voList);
    }

    @Override
    public InventoryOutboundVO getOutboundRecordById(Long id) {
        InventoryOutbound record = inventoryOutboundMapper.selectById(id);
        return record != null ? convertToVO(record) : null;
    }

    @Override
    @Transactional
    public void addOutboundRecord(InventoryOutboundDTO outboundDTO) {
        InventoryOutbound record = new InventoryOutbound();
        BeanUtils.copyProperties(outboundDTO, record);
        inventoryOutboundMapper.insert(record);
        log.info("新增出库记录成功: {}", record.getBatchNo());
    }

    @Override
    @Transactional
    public void updateOutboundRecord(InventoryOutboundDTO outboundDTO) {
        InventoryOutbound record = new InventoryOutbound();
        BeanUtils.copyProperties(outboundDTO, record);
        inventoryOutboundMapper.updateById(record);
        log.info("修改出库记录成功: {}", record.getBatchNo());
    }

    @Override
    @Transactional
    public void deleteOutboundRecord(Long id) {
        inventoryOutboundMapper.deleteById(id);
        log.info("删除出库记录成功: {}", id);
    }

    @Override
    @Transactional
    public void batchDeleteOutboundRecords(List<Long> ids) {
        inventoryOutboundMapper.deleteBatchIds(ids);
        log.info("批量删除出库记录成功: {}", ids);
    }

    @Override
    @Transactional
    public void approve(Long id, String approveRemark) {
        InventoryOutbound record = inventoryOutboundMapper.selectById(id);
        if (record == null) {
            return;
        }
        record.setStatus(1); // 审批通过
        record.setRemark(approveRemark);
        inventoryOutboundMapper.updateById(record);
    }

    @Override
    @Transactional
    public void confirm(Long id, String confirmRemark) {
        InventoryOutbound record = inventoryOutboundMapper.selectById(id);
        if (record == null) {
            log.warn("出库记录不存在，ID: {}", id);
            return;
        }

        // 检查状态，只有已审核状态的才能确认出库
        if (record.getStatus() != 1) {
            log.warn("出库记录状态不正确，无法确认出库。ID: {}, 当前状态: {}", id, record.getStatus());
            throw new RuntimeException("只有已审核状态的出库单才能确认出库");
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

        // 查询库存记录
        Inventory inventory = inventoryMapper.selectBySeedIdAndBatchIdAndWarehouseId(seedId, batchId, warehouseId);
        if (inventory == null || inventory.getAvailableQuantity() < quantity) {
            log.error("库存不足或不存在。种子ID={}, 批次ID={}, 仓库ID={}, 需要数量={}, 可用数量={}",
                    seedId, batchId, warehouseId, quantity,
                    inventory != null ? inventory.getAvailableQuantity() : 0);
            throw new RuntimeException("库存不足，无法出库");
        }

        // 扣减库存数量
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventoryMapper.updateById(inventory);

        // 更新出库记录状态为已出库
        record.setStatus(2); // 已出库（状态码：0-待审核，1-已审核，2-已出库，3-已取消）
        record.setRemark(confirmRemark);
        inventoryOutboundMapper.updateById(record);

        log.info("确认出库成功：出库记录ID={}, 种子ID={}, 批次ID={}, 仓库ID={}, 数量={}",
                id, seedId, batchId, warehouseId, quantity);
    }

    @Override
    @Transactional
    public void cancel(Long id, String cancelReason) {
        InventoryOutbound record = inventoryOutboundMapper.selectById(id);
        if (record == null) {
            return;
        }
        record.setStatus(0); // 取消/草稿
        record.setRemark(cancelReason);
        inventoryOutboundMapper.updateById(record);
    }

    private InventoryOutboundVO convertToVO(InventoryOutbound record) {
        InventoryOutboundVO vo = new InventoryOutboundVO();
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