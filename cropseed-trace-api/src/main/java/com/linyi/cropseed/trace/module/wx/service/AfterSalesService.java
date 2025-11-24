package com.linyi.cropseed.trace.module.wx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.cropseed.trace.module.wx.model.dto.AfterSalesApplyDTO;
import com.linyi.cropseed.trace.module.wx.model.entity.AfterSales;
import com.linyi.cropseed.trace.module.wx.model.vo.AfterSalesVO;

import java.util.List;

/**
 * 售后服务接口
 */
public interface AfterSalesService extends IService<AfterSales> {

    /**
     * 用户售后列表
     */
    List<AfterSalesVO> listByUser(Long userId, Integer status);

    /**
     * 申请售后
     */
    AfterSalesVO apply(Long userId, AfterSalesApplyDTO dto);

    /**
     * 获取详情
     */
    AfterSalesVO detail(Long userId, Long afterSalesId);

    /**
     * 撤销售后
     */
    void cancel(Long userId, Long afterSalesId);
}
