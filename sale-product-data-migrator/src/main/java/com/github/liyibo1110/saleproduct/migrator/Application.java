package com.github.liyibo1110.saleproduct.migrator;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 服务启动入口。
 * @author liyibo
 * @date 2026-07-13 14:36
 */
@SpringBootApplication
@EnableDubbo
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
