package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统操作日志Mapper
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}
