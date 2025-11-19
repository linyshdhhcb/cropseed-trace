package com.linyi.cropseed.trace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linyi.cropseed.trace.entity.TraceQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消费者溯源查询记录表 Mapper 接口
 * 
 * @author linyi
 * @since 2024-01-01
 */
@Mapper
public interface TraceQueryMapper extends BaseMapper<TraceQuery> {
    
    /**
     * 统计查询次数
     * @param traceCode 溯源码
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 查询次数
     */
    Long countByTraceCodeAndTime(@Param("traceCode") String traceCode, 
                                @Param("startTime") LocalDateTime startTime, 
                                @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询热门溯源码
     * @param limit 限制数量
     * @return 热门溯源码列表
     */
    List<Map<String, Object>> selectHotTraceCodes(@Param("limit") Integer limit);
    
    /**
     * 查询统计数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    Map<String, Object> selectStatistics(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
}
