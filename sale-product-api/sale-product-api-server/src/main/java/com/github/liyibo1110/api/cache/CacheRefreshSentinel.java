package com.github.liyibo1110.api.cache;

import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 缓存主动刷新哨兵。
 * 在读取时检测Redis Key的剩余TTL，当TTL低于阈值时异步触发刷新，
 * 保证缓存永远不会真正过期。仅用于来自外部接口的维度数据。
 * @author liyibo
 * @date 2026-07-23 14:24
 */
@Component
public class CacheRefreshSentinel {

    private static final Logger log = LoggerFactory.getLogger(CacheRefreshSentinel.class);

    /** 刷新阈值：剩余TTL低于原始TTL的20%时触发 */
    private static final double REFRESH_THRESHOLD = 0.2;

    /** 正在刷新中的Key集合，用于CAS防重复 */
    private final ConcurrentHashMap<String, Boolean> refreshingKeys = new ConcurrentHashMap<>();

    private final ExecutorService refreshExecutor;

    public CacheRefreshSentinel() {
        refreshExecutor = new ThreadPoolExecutor(2, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(256),
                r -> {
                    Thread t = new Thread(r, "cache-refresh");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.DiscardPolicy());
    }

    /**
     * 判断是否需要刷新，并在需要时异步执行
     */
    public void refreshIfNeeded(String redisKey, long remainTtlSeconds,
                                long originalTtlSeconds, Runnable refreshAction) {
        long threshold = (long) (originalTtlSeconds * REFRESH_THRESHOLD);
        if (remainTtlSeconds > threshold) {
            // TTL还很充足，不需要刷新
            return;
        }
        // CAS：同一个Key只有一个线程在刷新
        if (refreshingKeys.putIfAbsent(redisKey, Boolean.TRUE) != null) {
            // 已经有线程在刷新这个Key了
            return;
        }
        refreshExecutor.submit(() -> {
            try {
                refreshAction.run();
            } catch (Exception e) {
                StructuredLog.error(log)
                        .message("Cache refresh failed")
                        .put("redisKey", redisKey)
                        .exception(e)
                        .log();
            } finally {
                refreshingKeys.remove(redisKey);
            }
        });
    }
}
