package com.github.liyibo1110.api.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * 热Key检测器。
 * 基于Caffeine本地计数，统计每个merchandiseCode在最近10秒内的访问次数。
 * 超过阈值的标记为热Key。
 * @author liyibo
 * @date 2026-07-23 13:35
 */
@Component
public class HotKeyDetector {

    /** 滑动窗口：最近10秒的访问计数 */
    private final LoadingCache<String, LongAdder> accessCounter;

    /** 阈值：10秒内超过500次访问认为是热Key */
    private static final long HOT_THRESHOLD = 500;

    public HotKeyDetector() {
        this.accessCounter = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .maximumSize(100_000)
                .build(key -> new LongAdder());
    }

    /**
     * 记录一次访问
     */
    public void recordAccess(String merchandiseCode) {
        accessCounter.get(merchandiseCode).increment();
    }

    /**
     * 判断是否为热Key
     */
    public boolean isHotKey(String merchandiseCode) {
        LongAdder counter = accessCounter.getIfPresent(merchandiseCode);
        if (counter == null)
            return false;

        return counter.sum() >= HOT_THRESHOLD;
    }
}
