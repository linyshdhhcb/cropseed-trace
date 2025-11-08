package com.linyi.cropseed.trace.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.common.util.IdGenerator;
import com.linyi.cropseed.trace.dto.AfterSalesApplyDTO;
import com.linyi.cropseed.trace.entity.AfterSales;
import com.linyi.cropseed.trace.entity.OrderInfo;
import com.linyi.cropseed.trace.mapper.AfterSalesMapper;
import com.linyi.cropseed.trace.mapper.OrderInfoMapper;
import com.linyi.cropseed.trace.service.AfterSalesService;
import com.linyi.cropseed.trace.vo.AfterSalesVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 售后服务实现
 */
@Service
@RequiredArgsConstructor
public class AfterSalesServiceImpl extends ServiceImpl<AfterSalesMapper, AfterSales> implements AfterSalesService {

    private final AfterSalesMapper afterSalesMapper;
    private final OrderInfoMapper orderInfoMapper;

    @Override
    public List<AfterSalesVO> listByUser(Long userId, Integer status) {
        LambdaQueryWrapper<AfterSales> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AfterSales::getUserId, userId)
                .eq(status != null, AfterSales::getStatus, status)
                .orderByDesc(AfterSales::getCreateTime);
        List<AfterSales> list = afterSalesMapper.selectList(wrapper);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AfterSalesVO apply(Long userId, AfterSalesApplyDTO dto) {
        OrderInfo orderInfo = orderInfoMapper.selectById(dto.getOrderId());
        if (orderInfo == null || !orderInfo.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST, "订单不存在或无权操作");
        }

        AfterSales entity = new AfterSales();
        entity.setAfterSalesNo(IdGenerator.generateAfterSalesNo());
        entity.setOrderId(dto.getOrderId());
        entity.setUserId(userId);
        entity.setType(dto.getType());
        entity.setReason(dto.getReason());
        entity.setStatus(0);
        if (CollUtil.isNotEmpty(dto.getEvidenceImages())) {
            entity.setEvidenceImages(JSONUtil.toJsonStr(dto.getEvidenceImages()));
        }

        this.save(entity);
        return convertToVO(entity);
    }

    @Override
    public AfterSalesVO detail(Long userId, Long afterSalesId) {
        AfterSales entity = this.getById(afterSalesId);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "售后记录不存在");
        }
        return convertToVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long afterSalesId) {
        AfterSales entity = this.getById(afterSalesId);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "售后记录不存在");
        }
        if (entity.getStatus() != null && entity.getStatus() != 0) {
            throw new BusinessException("当前状态不可撤销");
        }

        entity.setStatus(3);
        entity.setProcessRemark("用户撤回售后申请");
        this.updateById(entity);
    }

    private AfterSalesVO convertToVO(AfterSales entity) {
        AfterSalesVO vo = new AfterSalesVO();
        vo.setId(entity.getId());
        vo.setAfterSalesNo(entity.getAfterSalesNo());
        vo.setOrderId(entity.getOrderId());
        vo.setType(entity.getType());
        vo.setReason(entity.getReason());
        vo.setStatus(entity.getStatus());
        vo.setRefundAmount(entity.getRefundAmount());
        vo.setProcessRemark(entity.getProcessRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setProcessTime(entity.getProcessTime());
        if (StrUtil.isNotBlank(entity.getEvidenceImages()) && JSONUtil.isTypeJSONArray(entity.getEvidenceImages())) {
            vo.setEvidenceImages(JSONUtil.parseArray(entity.getEvidenceImages()).toList(String.class));
        }
        return vo;
    }
}
