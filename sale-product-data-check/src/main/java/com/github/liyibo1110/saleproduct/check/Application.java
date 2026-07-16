package com.github.liyibo1110.saleproduct.check;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 服务启动入口。
 * @author liyibo
 * @date 2026-07-15 14:31
 */
@SpringBootApplication
@MapperScan("com.github.liyibo1110.saleproduct.check.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}