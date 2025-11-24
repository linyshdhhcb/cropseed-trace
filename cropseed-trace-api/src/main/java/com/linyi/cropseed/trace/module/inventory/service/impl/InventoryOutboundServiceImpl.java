package com.linyi.cropseed.trace.module.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.module.inventory.model.dto.InventoryOutboundDTO;
import com.linyi.cropseed.trace.module.inventory.model.entity.Inventory;
import com.linyi.cropseed.trace.module.inventory.model.entity.InventoryOutbound;
import com.linyi.cropseed.trace.module.seed.model.entity.SeedBatch;
import com.linyi.cropseed.trace.entity.Warehouse;
import com.linyi.cropseed.trace.module.inventory.mapper.InventoryMapper;
import com.linyi.cropseed.trace.module.inventory.mapper.InventoryOutboundMapper;
import com.linyi.cropseed.trace.module.seed.mapper.SeedBatchMapper;
import com.linyi.cropseed.trace.module.inventory.mapper.WarehouseMapper;
import com.linyi.cropseed.trace.module.inventory.service.InventoryOutboundService;
import com.linyi.cropseed.trace.module.inventory.model.vo.InventoryOutboundVO;
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
        // 验证批次是否存在
        SeedBatch seedBatch = seedBatchMapper.selectById(outboundDTO.getBatchId());
        if (seedBatch == null) {
            throw new RuntimeException("种子批次不存在，批次ID: " + outboundDTO.getBatchId());
        }

        // 验证库存是否充足
        Inventory inventory = inventoryMapper.selectBySeedIdAndBatchIdAndWarehouseId(
                outboundDTO.getSeedId(),
                outboundDTO.getBatchId(),
                outboundDTO.getWarehouseId());
        if (inventory == null || inventory.getAvailableQuantity() < outboundDTO.getQuantity()) {
            throw new RuntimeException("库存不足，无法出库。可用数量: " +
                    (inventory != null ? inventory.getAvailableQuantity() : 0));
        }

        InventoryOutbound record = new InventoryOutbound();

        BeanUtils.copyProperties(outboundDTO, record);
        record.setSeedBatchId(outboundDTO.getBatchId());
        record.setBatchNo(seedBatch.getBatchNo());
        record.setStatus(0); // 待审核
        record.setRemark(outboundDTO.getRemarks());

        inventoryOutboundMapper.insert(record);

        // 锁定库存：增加锁定数量，减少可用数量
        inventory.setLockedQuantity((inventory.getLockedQuantity() == null ? 0 : inventory.getLockedQuantity())
                + outboundDTO.getQuantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - outboundDTO.getQuantity());
        inventoryMapper.updateById(inventory);

        log.info("新增出库记录成功: 批次号={}, 数量={}, 已锁定库存", record.getBatchNo(), record.getQuantity());
    }

    @Override
    @Transactional
    public void updateOutboundRecord(InventoryOutboundDTO outboundDTO) {
        if (outboundDTO.getId() == null) {
            throw new RuntimeException("出库记录ID不能为空");
        }

        // 检查记录是否存在
        InventoryOutbound existingRecord = inventoryOutboundMapper.selectById(outboundDTO.getId());
        if (existingRecord == null) {
            throw new RuntimeException("出库记录不存在，ID: " + outboundDTO.getId());
        }

        // 如果状态不是待审核，不允许修改
        if (existingRecord.getStatus() != null && existingRecord.getStatus() != 0) {
            throw new RuntimeException("只有待审核状态的出库单才能修改");
        }

        // 验证批次是否存在
        SeedBatch seedBatch = seedBatchMapper.selectById(outboundDTO.getBatchId());
        if (seedBatch == null) {
            throw new RuntimeException("种子批次不存在，批次ID: " + outboundDTO.getBatchId());
        }

        // 验证库存是否充足并调整锁定库存（如果数量有变化）
        if (!existingRecord.getQuantity().equals(outboundDTO.getQuantity())) {
            Inventory inventory = inventoryMapper.selectBySeedIdAndBatchIdAndWarehouseId(
                    outboundDTO.getSeedId(),
                    outboundDTO.getBatchId(),
                    outboundDTO.getWarehouseId());
            if (inventory == null) {
                throw new RuntimeException("库存记录不存在");
            }

            int quantityDiff = outboundDTO.getQuantity() - existingRecord.getQuantity();
            if (quantityDiff > 0) {
                // 数量增加，需要增加锁定库存
                if (inventory.getAvailableQuantity() < quantityDiff) {
                    throw new RuntimeException("库存不足，无法修改。可用数量: " + inventory.getAvailableQuantity());
                }
                inventory.setLockedQuantity(
                        (inventory.getLockedQuantity() == null ? 0 : inventory.getLockedQuantity()) + quantityDiff);
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantityDiff);
            } else {
                // 数量减少，需要释放锁定库存
                int currentLocked = inventory.getLockedQuantity() == null ? 0 : inventory.getLockedQuantity();
                int releaseQuantity = Math.abs(quantityDiff);
                if (currentLocked < releaseQuantity) {
                    log.warn("锁定库存数量异常: 当前锁定={}, 需要释放={}", currentLocked, releaseQuantity);
                }
                inventory.setLockedQuantity(Math.max(0, currentLocked - releaseQuantity));
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() + releaseQuantity);
            }
            inventoryMapper.updateById(inventory);
            log.info("更新出库记录数量变化，已调整锁定库存: 原数量={}, 新数量={}, 变化={}",
                    existingRecord.getQuantity(), outboundDTO.getQuantity(), quantityDiff);
        }

        InventoryOutbound record = new InventoryOutbound();
        // 复制基本属性
        BeanUtils.copyProperties(outboundDTO, record);

        // 手动设置字段映射
        record.setId(outboundDTO.getId()); // 设置ID
        record.setSeedBatchId(outboundDTO.getBatchId()); // batchId -> seedBatchId
        record.setBatchNo(seedBatch.getBatchNo()); // 从SeedBatch获取批次号
        record.setStatus(existingRecord.getStatus()); // 保持原有状态
        record.setRemark(outboundDTO.getRemarks()); // remarks -> remark

        inventoryOutboundMapper.updateById(record);
        log.info("修改出库记录成功: 批次号={}, 数量={}", record.getBatchNo(), record.getQuantity());
    }

    @Override
    @Transactional
    public void deleteOutboundRecord(Long id) {
        InventoryOutbound record = inventoryOutboundMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("出库记录不存在，ID: " + id);
        }

        // 如果状态是待审核，需要释放锁定库存
        if (record.getStatus() != null && record.getStatus() == 0) {
            // 获取种子批次信息
            SeedBatch seedBatch = seedBatchMapper.selectById(record.getSeedBatchId());
            if (seedBatch != null && seedBatch.getSeedId() != null) {
                Inventory inventory = inventoryMapper.selectBySeedIdAndBatchIdAndWarehouseId(
                        seedBatch.getSeedId(),
                        record.getSeedBatchId(),
                        record.getWarehouseId());
                if (inventory != null) {
                    // 释放锁定库存
                    int currentLocked = inventory.getLockedQuantity() == null ? 0 : inventory.getLockedQuantity();
                    int quantity = record.getQuantity();
                    if (currentLocked >= quantity) {
                        inventory.setLockedQuantity(currentLocked - quantity);
                        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
                        inventoryMapper.updateById(inventory);
                        log.info("删除待审核出库记录，已释放锁定库存: ID={}, 数量={}", id, quantity);
                    } else {
                        log.warn("锁定库存数量异常: 当前锁定={}, 需要释放={}", currentLocked, quantity);
                    }
                }
            }
        }

        inventoryOutboundMapper.deleteById(id);
        log.info("删除出库记录成功: {}", id);
    }

    @Override
    @Transactional
    public void batchDeleteOutboundRecords(List<Long> ids) {
        ids.forEach(id -> inventoryOutboundMapper.deleteById(id));
        log.info("批量删除出库记录成功: {}", ids);
    }

    @Override
    @Transactional
    public void approve(Long id, Boolean approved, String approveRemark) {
        InventoryOutbound record = inventoryOutboundMapper.selectById(id);
        if (record == null) {
            throw new RuntimeException("出库记录不存在，ID: " + id);
        }

        // 只有待审核状态的才能审批
        if (record.getStatus() != 0) {
            throw new RuntimeException("只有待审核状态的出库单才能审批。当前状态: " + record.getStatus());
        }

        // 获取种子批次信息
        SeedBatch seedBatch = seedBatchMapper.selectById(record.getSeedBatchId());
        if (seedBatch == null || seedBatch.getSeedId() == null) {
            throw new RuntimeException("种子批次不存在或缺少种子信息，批次ID: " + record.getSeedBatchId());
        }

        Long seedId = seedBatch.getSeedId();
        Long batchId = record.getSeedBatchId();
        Long warehouseId = record.getWarehouseId();
        Integer quantity = record.getQuantity();

        // 查询库存记录
        Inventory inventory = inventoryMapper.selectBySeedIdAndBatchIdAndWarehouseId(seedId, batchId, warehouseId);
        if (inventory == null) {
            throw new RuntimeException("库存记录不存在");
        }

        if (approved) {
            // 审批通过：状态变为已审核（1），保持锁定库存
            record.setStatus(1);
            record.setRemark(approveRemark);
            inventoryOutboundMapper.updateById(record);
            log.info("审批通过出库单: ID={}, 批次号={}, 数量={}", id, record.getBatchNo(), quantity);
        } else {
            // 审批不通过：状态变为已取消（3），释放锁定库存
            record.setStatus(3);
            record.setRemark(approveRemark != null ? approveRemark : "审批不通过");
            inventoryOutboundMapper.updateById(record);

            // 释放锁定库存：减少锁定数量，增加可用数量
            int currentLocked = inventory.getLockedQuantity() == null ? 0 : inventory.getLockedQuantity();
            if (currentLocked < quantity) {
                log.warn("锁定库存数量异常: 当前锁定={}, 需要释放={}", currentLocked, quantity);
            }
            inventory.setLockedQuantity(Math.max(0, currentLocked - quantity));
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
            inventoryMapper.updateById(inventory);

            log.info("审批不通过出库单: ID={}, 批次号={}, 数量={}, 已释放锁定库存", id, record.getBatchNo(), quantity);
        }
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

        // 扣减库存数量：减少总库存和锁定库存
        int currentLocked = inventory.getLockedQuantity() == null ? 0 : inventory.getLockedQuantity();
        if (currentLocked < quantity) {
            log.warn("锁定库存数量异常: 当前锁定={}, 需要扣减={}", currentLocked, quantity);
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setLockedQuantity(Math.max(0, currentLocked - quantity));
        // availableQuantity不需要变动，因为之前已经锁定过了
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
            throw new RuntimeException("出库记录不存在，ID: " + id);
        }

        // 只有待审核或已审核状态的才能取消
        if (record.getStatus() != 0 && record.getStatus() != 1) {
            throw new RuntimeException("只有待审核或已审核状态的出库单才能取消。当前状态: " + record.getStatus());
        }

        // 获取种子批次信息
        SeedBatch seedBatch = seedBatchMapper.selectById(record.getSeedBatchId());
        if (seedBatch == null || seedBatch.getSeedId() == null) {
            throw new RuntimeException("种子批次不存在或缺少种子信息，批次ID: " + record.getSeedBatchId());
        }

        Long seedId = seedBatch.getSeedId();
        Long batchId = record.getSeedBatchId();
        Long warehouseId = record.getWarehouseId();
        Integer quantity = record.getQuantity();

        // 查询库存记录
        Inventory inventory = inventoryMapper.selectBySeedIdAndBatchIdAndWarehouseId(seedId, batchId, warehouseId);
        if (inventory == null) {
            throw new RuntimeException("库存记录不存在");
        }

        // 更新出库记录状态为已取消
        record.setStatus(3); // 已取消
        record.setRemark(cancelReason != null ? cancelReason : "已取消");
        inventoryOutboundMapper.updateById(record);

        // 释放锁定库存：减少锁定数量，增加可用数量
        int currentLocked = inventory.getLockedQuantity() == null ? 0 : inventory.getLockedQuantity();
        if (currentLocked < quantity) {
            log.warn("锁定库存数量异常: 当前锁定={}, 需要释放={}", currentLocked, quantity);
        }
        inventory.setLockedQuantity(Math.max(0, currentLocked - quantity));
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventoryMapper.updateById(inventory);

        log.info("取消出库单成功: ID={}, 批次号={}, 数量={}, 已释放锁定库存", id, record.getBatchNo(), quantity);
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
