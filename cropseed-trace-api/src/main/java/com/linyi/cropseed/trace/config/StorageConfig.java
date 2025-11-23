package com.linyi.cropseed.trace.config;

import com.linyi.cropseed.trace.infrastructure.storage.StorageService;
import com.linyi.cropseed.trace.config.properties.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 对象存储配置类
 * 根据配置动态选择存储服务实现
 *
 * @author LinYi
 * @since 2025-11-23
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.enabled", havingValue = "true", matchIfMissing = true)
public class StorageConfig {

    private final StorageProperties storageProperties;
    private final ApplicationContext applicationContext;

    /**
     * 主要的存储服务Bean
     * 根据配置动态选择具体实现
     */
    @Bean
    @Primary
    public StorageService storageService() {
        String type = storageProperties.getType();
        log.info("初始化对象存储服务，类型：{}", type);

        StorageService storageService;
        try {
            // 根据存储类型获取对应的Bean
            String beanName = type + "StorageService";
            storageService = applicationContext.getBean(beanName, StorageService.class);
            log.info("成功加载 {} 存储服务实现", type.toUpperCase());
        } catch (Exception e) {
            log.warn("无法加载 {} 存储服务，将尝试使用默认的MinIO服务", type);
            try {
                storageService = applicationContext.getBean("minioStorageService", StorageService.class);
                log.info("已降级使用MinIO存储服务");
            } catch (Exception ex) {
                log.error("无法加载任何存储服务实现", ex);
                throw new RuntimeException("对象存储服务初始化失败", ex);
            }
        }

        return storageService;
    }
}
