package com.github.liyibo1110.saleproduct.check.interfacecheck;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author liyibo
 * @date 2026-07-16 14:32
 */
@Component
@ConfigurationProperties(prefix = "interface-check")
@Data
public class InterfaceCheckConfig {

    private boolean enabled = true;
    private String javaServiceBaseUrl;
    private int sampleRate = 100;   // 100代表100%进行
    private int maxDailyCheck = 100000;

    /**
     * 根据采样率决定是否执行本次校验。
     */
    public boolean shouldCheck() {
        if (!enabled)
            return false;

        if (sampleRate >= 100)
            return true;

        return ThreadLocalRandom.current().nextInt(100) < sampleRate;
    }
}
