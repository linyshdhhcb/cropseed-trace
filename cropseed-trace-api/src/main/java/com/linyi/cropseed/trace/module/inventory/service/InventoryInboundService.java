package com.linyi.cropseed.trace.module.inventory.service;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.module.inventory.model.dto.InventoryInboundDTO;
import com.linyi.cropseed.trace.module.inventory.model.vo.InventoryInboundVO;

import java.util.List;

/**
 * 入库管理Service接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface InventoryInboundService {

    /**
     * 分页查询入库记录列表
     */
    PageResult<InventoryInboundVO> pageInboundRecords(PageQuery pageQuery, String batchNo, Long warehouseId,
            Integer status);

    /**
     * 根据ID查询入库记录详情
     */
    InventoryInboundVO getInboundRecordById(Long id);

    /**
     * 新增入库记录
     */
    void addInboundRecord(InventoryInboundDTO inboundDTO);

    /**
     * 修改入库记录
     */
    void updateInboundRecord(InventoryInboundDTO inboundDTO);

    /**
     * 删除入库记录
     */
    void deleteInboundRecord(Long id);

    /**
     * 批量删除入库记录
     */
    void batchDeleteInboundRecords(List<Long> ids);

    /**
     * 审批入库单
     */
    void approve(Long id, String approveRemark);

    /**
     * 确认入库单
     */
    void confirm(Long id, String confirmRemark);

    /**
     * 取消入库单
     */
    void cancel(Long id, String cancelReason);
}
