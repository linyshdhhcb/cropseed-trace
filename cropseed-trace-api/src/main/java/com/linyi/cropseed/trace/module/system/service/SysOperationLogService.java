package com.linyi.cropseed.trace.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.linyi.cropseed.trace.common.page.PageQuery;
import com.linyi.cropseed.trace.common.page.PageResult;
import com.linyi.cropseed.trace.module.system.model.dto.SysOperationLogQueryDTO;
import com.linyi.cropseed.trace.module.system.model.entity.SysOperationLog;
import com.linyi.cropseed.trace.module.system.model.vo.SysOperationLogVO;

import java.util.List;

/**
 * 操作日志服务接口
 *
 * @author LinYi
 * @since 2025-11-28
 */
public interface SysOperationLogService extends IService<SysOperationLog> {

    /**
     * 分页查询操作日志
     */
    PageResult<SysOperationLogVO> pageLogs(PageQuery pageQuery, SysOperationLogQueryDTO queryDTO);

    /**
     * 根据ID查询操作日志详情
     */
    SysOperationLogVO getLogById(Long id);

    /**
     * 保存操作日志
     */
    void saveLog(SysOperationLog log);

    /**
     * 删除操作日志
     */
    void deleteLog(Long id);

    /**
     * 批量删除操作日志
     */
    void batchDeleteLogs(List<Long> ids);

    /**
     * 清空操作日志
     */
    void clearLogs();
}
