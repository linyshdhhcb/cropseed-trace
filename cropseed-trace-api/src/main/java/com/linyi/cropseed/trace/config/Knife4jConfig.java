package com.linyi.cropseed.trace.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置
 * 
 * @author LinYi
 * @since 2025-10-25
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("农作物种质资源数字化溯源系统 API")
                        .version("1.0.0")
                        .description("集种子档案管理、库存仓储、订单交易与微信小程序商城于一体的数字化溯源系统")
                        .contact(new Contact()
                                .name("LinYi")
                                .email("linyi@cropseed.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    /**
     * 系统管理模块
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("01-系统管理")
                .pathsToMatch("/system/**")
                .build();
    }

    /**
     * 种子管理模块
     */
    @Bean
    public GroupedOpenApi seedApi() {
        return GroupedOpenApi.builder()
                .group("02-种子管理")
                .pathsToMatch("/seed/**")
                .build();
    }

    /**
     * 库存管理模块
     */
    @Bean
    public GroupedOpenApi inventoryApi() {
        return GroupedOpenApi.builder()
                .group("03-库存管理")
                .pathsToMatch("/inventory/**", "/warehouse/**")
                .build();
    }

    /**
     * 订单管理模块
     */
    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("04-订单管理")
                .pathsToMatch("/order/**")
                .build();
    }

    /**
     * 微信小程序模块
     */
    @Bean
    public GroupedOpenApi wechatApi() {
        return GroupedOpenApi.builder()
                .group("05-微信小程序")
                .pathsToMatch("/wechat/**", "/miniapp/**")
                .build();
    }

    /**
     * 推荐系统模块
     */
    @Bean
    public GroupedOpenApi recommendApi() {
        return GroupedOpenApi.builder()
                .group("06-推荐系统")
                .pathsToMatch("/recommend/**")
                .build();
    }

    /**
     * 文件管理模块
     */
    @Bean
    public GroupedOpenApi fileApi() {
        return GroupedOpenApi.builder()
                .group("07-文件管理")
                .pathsToMatch("/file/**")
                .build();
    }
}
