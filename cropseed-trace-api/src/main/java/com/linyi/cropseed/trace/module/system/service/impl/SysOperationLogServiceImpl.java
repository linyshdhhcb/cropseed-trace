package com.linyi.cropseed.trace.module.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linyi.cropseed.trace.common.exception.BusinessException;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.common.result.ResultCode;
import com.linyi.cropseed.trace.module.system.mapper.SysOperationLogMapper;
import com.linyi.cropseed.trace.module.system.model.dto.SysOperationLogQueryDTO;
import com.linyi.cropseed.trace.module.system.model.entity.SysOperationLog;
import com.linyi.cropseed.trace.module.system.model.vo.SysOperationLogVO;
import com.linyi.cropseed.trace.module.system.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 操作日志服务实现
 *
 * @author LinYi
 * @since 2025-11-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    @Override
    public PageResult<SysOperationLogVO> pageLogs(PageQuery pageQuery, SysOperationLogQueryDTO queryDTO) {
        Page<SysOperationLog> page = pageQuery.toMpPageWithOrder();

        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO != null) {
            wrapper.like(StrUtil.isNotBlank(queryDTO.getUsername()), SysOperationLog::getUsername, queryDTO.getUsername())
                    .like(StrUtil.isNotBlank(queryDTO.getModule()), SysOperationLog::getModule, queryDTO.getModule())
                    .like(StrUtil.isNotBlank(queryDTO.getContent()), SysOperationLog::getContent, queryDTO.getContent())
                    .eq(queryDTO.getStatus() != null, SysOperationLog::getStatus, queryDTO.getStatus())
                    .like(StrUtil.isNotBlank(queryDTO.getIp()), SysOperationLog::getIp, queryDTO.getIp())
                    .ge(queryDTO.getStartTime() != null, SysOperationLog::getCreateTime, queryDTO.getStartTime())
                    .le(queryDTO.getEndTime() != null, SysOperationLog::getCreateTime, queryDTO.getEndTime());
        }
        wrapper.orderByDesc(SysOperationLog::getCreateTime);

        Page<SysOperationLog> resultPage = this.page(page, wrapper);

        List<SysOperationLogVO> voList = resultPage.getRecords().stream()
                .map(log -> BeanUtil.copyProperties(log, SysOperationLogVO.class))
                .toList();

        return PageResult.of(resultPage, voList);
    }

    @Override
    public SysOperationLogVO getLogById(Long id) {
        SysOperationLog log = this.getById(id);
        if (log == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "操作日志不存在");
        }
        return BeanUtil.copyProperties(log, SysOperationLogVO.class);
    }

    @Async
    @Override
    public void saveLog(SysOperationLog operationLog) {
        try {
            this.save(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteLog(Long id) {
        if (!this.removeById(id)) {
            throw new BusinessException(ResultCode.DATA_OPERATION_FAIL, "删除操作日志失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchDeleteLogs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "日志ID列表不能为空");
        }
        if (!this.removeByIds(ids)) {
            throw new BusinessException(ResultCode.DATA_OPERATION_FAIL, "批量删除操作日志失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void clearLogs() {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        this.remove(wrapper);
    }
}
