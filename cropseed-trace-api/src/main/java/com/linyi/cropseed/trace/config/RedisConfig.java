package com.linyi.cropseed.trace.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.linyi.cropseed.trace.common.constant.CacheConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis配置
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
                Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // String的序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 配置序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // 默认缓存配置：30分钟过期
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(CacheConstants.DEFAULT_EXPIRE_TIME))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringRedisSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> CacheConstants.CACHE_PREFIX + cacheName + ":");

        // 针对不同缓存的个性化配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户相关缓存：30分钟
        cacheConfigurations.put(CacheConstants.CACHE_USER, 
            createCacheConfig(jackson2JsonRedisSerializer, CacheConstants.DEFAULT_EXPIRE_TIME));
        cacheConfigurations.put(CacheConstants.CACHE_USER_ROLES, 
            createCacheConfig(jackson2JsonRedisSerializer, CacheConstants.DEFAULT_EXPIRE_TIME));
        cacheConfigurations.put(CacheConstants.CACHE_USER_PERMISSIONS, 
            createCacheConfig(jackson2JsonRedisSerializer, CacheConstants.DEFAULT_EXPIRE_TIME));
        
        // 角色相关缓存：30分钟
        cacheConfigurations.put(CacheConstants.CACHE_ROLE, 
            createCacheConfig(jackson2JsonRedisSerializer, CacheConstants.DEFAULT_EXPIRE_TIME));
        cacheConfigurations.put(CacheConstants.CACHE_ROLE_MENUS, 
            createCacheConfig(jackson2JsonRedisSerializer, CacheConstants.DEFAULT_EXPIRE_TIME));
        cacheConfigurations.put(CacheConstants.CACHE_ROLE_ALL, 
            createCacheConfig(jackson2JsonRedisSerializer, CacheConstants.LONG_EXPIRE_TIME));
        
        // 菜单相关缓存：1小时（菜单变动较少）
        cacheConfigurations.put(CacheConstants.CACHE_MENU, 
            createCacheConfig(jackson2JsonRedisSerializer, CacheConstants.LONG_EXPIRE_TIME));
        cacheConfigurations.put(CacheConstants.CACHE_MENU_LIST, 
            createCacheConfig(jackson2JsonRedisSerializer, CacheConstants.DEFAULT_EXPIRE_TIME));
        cacheConfigurations.put(CacheConstants.CACHE_MENU_TREE, 
            createCacheConfig(jackson2JsonRedisSerializer, CacheConstants.LONG_EXPIRE_TIME));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * 创建缓存配置
     */
    private RedisCacheConfiguration createCacheConfig(
            Jackson2JsonRedisSerializer<Object> serializer, long ttlSeconds) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttlSeconds))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> CacheConstants.CACHE_PREFIX + cacheName + ":");
    }
}
