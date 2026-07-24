package com.github.liyibo1110.api.cache;

import org.springframework.stereotype.Component;

/**
 * OHC TTL动态策略。
 * 根据热Key检测结果返回不同的OHC缓存过期时间：
 * 普通Key 5分钟，热Key 20分钟。
 * @author liyibo
 * @date 2026-07-23 13:35
 */
@Component
public class OhcTtlStrategy {

    private final HotKeyDetector hotKeyDetector;

    /** 普通Key的OHC TTL：5分钟 */
    public static final long NORMAL_TTL_MS = 5 * 60 * 1000;

    /** 热Key的OHC TTL：20分钟 */
    public static final long HOT_TTL_MS = 20 * 60 * 1000;

    public OhcTtlStrategy(HotKeyDetector hotKeyDetector) {
        this.hotKeyDetector = hotKeyDetector;
    }

    public long getTtlMs(String merchandiseCode) {
        if (hotKeyDetector.isHotKey(merchandiseCode))
            return HOT_TTL_MS;

        return NORMAL_TTL_MS;
    }
}
