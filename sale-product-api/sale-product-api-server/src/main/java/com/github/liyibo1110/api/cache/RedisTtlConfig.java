package com.github.liyibo1110.api.cache;

import com.github.liyibo1110.api.enums.QueryOption;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liyibo
 * @date 2026-07-22 15:39
 */
@Component
@ConfigurationProperties(prefix = "merchandise.redis")
public class RedisTtlConfig {

    private Map<String, Long> ttl = new HashMap<>();

    public long getTtlSeconds(QueryOption option) {
        return ttl.getOrDefault(option.name().toLowerCase(), 1800L);
    }

    public Map<String, Long> getTtl() {
        return ttl;
    }

    public void setTtl(Map<String, Long> ttl) {
        this.ttl = ttl;
    }
}
