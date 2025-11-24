package com.linyi.cropseed.trace.module.inventory.service;

import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.module.inventory.model.dto.InventoryOutboundDTO;
import com.linyi.cropseed.trace.module.inventory.model.vo.InventoryOutboundVO;

import java.util.List;

/**
 * 出库管理Service接口
 *
 * @author LinYi
 * @since 2025-10-25
 */
public interface InventoryOutboundService {

    /**
     * 分页查询出库记录列表
     */
    PageResult<InventoryOutboundVO> pageOutboundRecords(PageQuery pageQuery, String batchNo, Long warehouseId,
            Integer status);

    /**
     * 根据ID查询出库记录详情
     */
    InventoryOutboundVO getOutboundRecordById(Long id);

    /**
     * 新增出库记录
     */
    void addOutboundRecord(InventoryOutboundDTO outboundDTO);

    /**
     * 修改出库记录
     */
    void updateOutboundRecord(InventoryOutboundDTO outboundDTO);

    /**
     * 删除出库记录
     */
    void deleteOutboundRecord(Long id);

    /**
     * 批量删除出库记录
     */
    void batchDeleteOutboundRecords(List<Long> ids);

    /**
     * 审批出库单
     *
     * @param id            出库记录ID
     * @param approved      true-审批通过，false-审批不通过
     * @param approveRemark 审批备注
     */
    void approve(Long id, Boolean approved, String approveRemark);

    /**
     * 确认出库单
     */
    void confirm(Long id, String confirmRemark);

    /**
     * 取消出库单
     */
    void cancel(Long id, String cancelReason);
}
