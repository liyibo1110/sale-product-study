package com.github.liyibo1110.api.cache;

import com.github.liyibo1110.api.enums.QueryOption;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * cache命中统计组件，基于micrometer。
 * @author liyibo
 * @date 2026-07-23 14:32
 */
@Component
public class CacheMetrics {

    private final MeterRegistry meterRegistry;

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    public CacheMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordRedisHit(QueryOption option) {
        getCounter("cache.redis.hit", option).increment();
    }

    public void recordRedisMiss(QueryOption option) {
        getCounter("cache.redis.miss", option).increment();
    }

    public void recordDbAccess(QueryOption option) {
        getCounter("cache.db.access", option).increment();
    }

    private Counter getCounter(String metricName, QueryOption option) {
        String key = metricName + "." + option.name();
        return counters.computeIfAbsent(key,
                k -> Counter.builder(metricName)
                        .tag("dimension", option.name().toLowerCase())
                        .register(meterRegistry));
    }
}
