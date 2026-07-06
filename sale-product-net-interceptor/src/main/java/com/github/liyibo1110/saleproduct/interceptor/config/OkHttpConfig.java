package com.github.liyibo1110.saleproduct.interceptor.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * OkHttpClient的自动配置。
 * @author liyibo
 * @date 2026-07-06 11:46
 */
@Configuration
public class OkHttpConfig {

    @Bean
    public OkHttpClient okHttpClient() {
        // 最多保持200个空闲连接，空闲5分钟后回收
        ConnectionPool pool = new ConnectionPool(200, 5, TimeUnit.MINUTES);
        return new OkHttpClient.Builder()
                .connectionPool(pool)
                .connectTimeout(3, TimeUnit.SECONDS)    // TCP连接超时
                .readTimeout(10, TimeUnit.SECONDS)      // 读取超时
                .writeTimeout(10, TimeUnit.SECONDS)     // 写入超时
                .build();
    }
}
