package com.linyi.cropseed.trace;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 农作物种质资源数字化溯源系统 - 主启动类
 *
 * @author LinYi
 * @since 2025-10-25
 */
@SpringBootApplication
@MapperScan("com.linyi.cropseed.trace.mapper")
@EnableAsync
@EnableScheduling
public class CropSeedTraceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CropSeedTraceApplication.class, args);
        System.out.println("""
                ========================================
                农作物种质资源数字化溯源系统启动成功！
                API文档地址: http://localhost:8085/doc.html
                ========================================
                """);
    }
}
