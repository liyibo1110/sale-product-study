package com.github.liyibo1110.api.cache.spring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liyibo
 * @date 2026-07-23 14:42
 */
@Data
@Component
@ConfigurationProperties(prefix = "ohc")
public class OhcCacheProperties {

    private Map<String, Long> capacity = new HashMap<>();
}
