package com.linyi.cropseed.trace.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson配置
 * 解决前端JavaScript数字精度问题：将Long类型序列化为字符串
 * 配置LocalDateTime序列化和反序列化格式
 *
 * @author LinYi
 * @since 2025-11-08
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    /**
     * 自定义ObjectMapper，确保Long类型序列化为字符串
     * 解决JavaScript Number类型精度丢失问题（JavaScript只能安全表示到2^53-1）
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        // 创建SimpleModule用于Long类型序列化
        SimpleModule simpleModule = new SimpleModule();
        // 将Long类型序列化为字符串
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        // 将long基本类型也序列化为字符串
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 创建JavaTimeModule用于LocalDateTime序列化
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));

        // 构建ObjectMapper并注册模块
        return builder
                .modules(simpleModule, javaTimeModule)
                .build();
    }
}
