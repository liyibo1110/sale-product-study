package com.github.liyibo1110.api.cache.spring;

import com.github.liyibo1110.api.cache.StringKeySerializer;
import org.caffinitas.ohc.OHCache;
import org.caffinitas.ohc.OHCacheBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于Spring Cache的CacheManager接口实现封装，内部实现基于OHC。
 * @author liyibo
 * @date 2026-07-23 14:43
 */
public class OhcCacheManager implements CacheManager {

    private final ConcurrentMap<String, OhcCache> caches = new ConcurrentHashMap<>();

    private final Map<String, Long> capacityConfig;

    public OhcCacheManager(Map<String, Long> capacityConfig) {
        this.capacityConfig = capacityConfig;
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name, this::createCache);
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(caches.keySet());
    }

    private OhcCache createCache(String name) {
        long capacity = capacityConfig.getOrDefault(name, 256L * 1024 * 1024);
        OHCache<String, byte[]> ohCache =
                OHCacheBuilder.<String, byte[]>newBuilder()
                        .keySerializer(new StringKeySerializer())
                        .valueSerializer(new ByteArraySerializer())
                        .capacity(capacity)
                        .segmentCount(64)
                        .timeouts(true)
                        .defaultTTLmillis(300_000)
                        .build();
        return new OhcCache(name, ohCache);
    }
}
