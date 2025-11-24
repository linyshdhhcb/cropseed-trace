package com.linyi.cropseed.trace.module.trace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.linyi.cropseed.trace.module.trace.model.entity.TraceCodeConfig;
import com.linyi.cropseed.trace.module.trace.mapper.TraceCodeConfigMapper;
import com.linyi.cropseed.trace.module.trace.service.TraceCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 溯源码生成服务实现类
 *
 * @author linyi
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TraceCodeServiceImpl implements TraceCodeService {

    private final TraceCodeConfigMapper traceCodeConfigMapper;
    private final RedissonClient redissonClient;

    // 溯源码正则表达式：地区代码(2位) + 年份(4位) + 序号(6位)
    private static final Pattern TRACE_CODE_PATTERN = Pattern.compile("^([A-Z]{2})(\\d{4})(\\d{6})$");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateTraceCode(String regionCode) {
        if (!StringUtils.hasText(regionCode)) {
            throw new IllegalArgumentException("地区代码不能为空");
        }

        RLock lock = redissonClient.getLock("trace:code:generate:" + regionCode);
        try {
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {

                // 1. 查询或创建配置
                TraceCodeConfig config = getOrCreateConfig(regionCode);

                // 2. 生成当前年份的序号
                int currentYear = Year.now().getValue();
                Long nextNumber = config.getCurrentNumber() + 1;

                // 3. 构建溯源码
                String traceCode = String.format("%s%d%06d",
                    config.getCodePrefix(), currentYear, nextNumber);

                // 4. 更新配置中的当前序号
                LambdaUpdateWrapper<TraceCodeConfig> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(TraceCodeConfig::getRegionCode, regionCode)
                           .set(TraceCodeConfig::getCurrentNumber, nextNumber)
                           .set(TraceCodeConfig::getUpdateTime, LocalDateTime.now());

                traceCodeConfigMapper.update(null, updateWrapper);

                log.info("生成溯源码成功: regionCode={}, traceCode={}", regionCode, traceCode);
                return traceCode;

            } else {
                throw new RuntimeException("获取溯源码生成锁超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("溯源码生成被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public boolean validateTraceCode(String traceCode) {
        if (!StringUtils.hasText(traceCode)) {
            return false;
        }

        Matcher matcher = TRACE_CODE_PATTERN.matcher(traceCode);
        if (!matcher.matches()) {
            log.warn("溯源码格式不正确: {}", traceCode);
            return false;
        }

        // 验证年份是否合理（2020-2050年）
        String yearStr = matcher.group(2);
        int year = Integer.parseInt(yearStr);
        if (year < 2020 || year > 2050) {
            log.warn("溯源码年份不合理: traceCode={}, year={}", traceCode, year);
            return false;
        }

        return true;
    }

    @Override
    public TraceCodeInfo parseTraceCode(String traceCode) {
        if (!validateTraceCode(traceCode)) {
            throw new IllegalArgumentException("溯源码格式不正确: " + traceCode);
        }

        Matcher matcher = TRACE_CODE_PATTERN.matcher(traceCode);
        if (matcher.matches()) {
            String regionCode = matcher.group(1);
            int year = Integer.parseInt(matcher.group(2));
            long number = Long.parseLong(matcher.group(3));

            // 查询地区名称
            TraceCodeConfig config = getConfigByRegionCode(regionCode);
            String regionName = config != null ? config.getRegionName() : "未知地区";

            return new TraceCodeInfo(regionCode, regionName, year, number);
        }

        throw new IllegalArgumentException("溯源码解析失败: " + traceCode);
    }

    /**
     * 获取或创建地区配置
     */
    private TraceCodeConfig getOrCreateConfig(String regionCode) {
        LambdaQueryWrapper<TraceCodeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceCodeConfig::getRegionCode, regionCode)
               .eq(TraceCodeConfig::getStatus, 1);

        TraceCodeConfig config = traceCodeConfigMapper.selectOne(wrapper);

        if (config == null) {
            // 创建默认配置
            config = new TraceCodeConfig();
            config.setRegionCode(regionCode);
            config.setRegionName(getDefaultRegionName(regionCode));
            config.setCodePrefix(regionCode);
            config.setCurrentNumber(0L);
            config.setCodeFormat("{prefix}{year}{number:06d}");
            config.setStatus(1);
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());

            traceCodeConfigMapper.insert(config);
            log.info("创建溯源码配置: regionCode={}", regionCode);
        }

        return config;
    }

    /**
     * 根据地区代码获取配置
     */
    private TraceCodeConfig getConfigByRegionCode(String regionCode) {
        LambdaQueryWrapper<TraceCodeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TraceCodeConfig::getRegionCode, regionCode);
        return traceCodeConfigMapper.selectOne(wrapper);
    }

    /**
     * 获取默认地区名称
     */
    private String getDefaultRegionName(String regionCode) {
        // 这里可以根据实际需要配置地区映射
        switch (regionCode) {
            case "BJ": return "北京市";
            case "SH": return "上海市";
            case "GZ": return "广州市";
            case "SZ": return "深圳市";
            case "HZ": return "杭州市";
            case "CD": return "成都市";
            case "WH": return "武汉市";
            case "XA": return "西安市";
            default: return regionCode + "地区";
        }
    }
}
