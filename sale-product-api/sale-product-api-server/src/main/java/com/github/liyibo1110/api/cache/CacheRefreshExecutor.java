package com.github.liyibo1110.api.cache;

import com.github.liyibo1110.api.enums.QueryOption;
import com.github.liyibo1110.api.loader.DataLoader;
import com.github.liyibo1110.api.mapper.MerchandiseReadMapper;
import com.github.liyibo1110.saleproduct.admin.api.dto.MerchandiseChangeMessage;
import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 在后台修改了商品的字段，要主动发消息来通知redis重新刷新特定cache。
 * @author liyibo
 * @date 2026-07-23 14:29
 */
@Component
public class CacheRefreshExecutor {

    private static final Logger log = LoggerFactory.getLogger(CacheRefreshExecutor.class);

    private static final int MAX_RETRY = 5;

    private static final long[] RETRY_DELAYS = { 1000, 5000, 10000, 30000, 60000 };

    private final MerchandiseReadMapper merchandiseReadMapper;
    private final RedisCacheService redisCacheService;
    private final Map<QueryOption, DataLoader<?>> loaderMap;
    private final ScheduledExecutorService retryScheduler;
    private final RateLimiter rateLimiter;

    public CacheRefreshExecutor(MerchandiseReadMapper merchandiseReadMapper,
                                RedisCacheService redisCacheService,
                                List<DataLoader<?>> loaders) {
        this.merchandiseReadMapper = merchandiseReadMapper;
        this.redisCacheService = redisCacheService;
        this.loaderMap = loaders.stream().collect(Collectors.toMap(DataLoader::supportedOption, Function.identity()));
        this.retryScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "cache-refresh-retry");
            t.setDaemon(true);
            return t;
        });
        this.rateLimiter = RateLimiter.create(100);
    }

    public void execute(MerchandiseChangeMessage message) {
        rateLimiter.acquire();
        executeWithRetry(message, 0);
    }

    private void executeWithRetry(MerchandiseChangeMessage message, int retryCount) {
        String merchandiseCode = message.getMerchandiseCode();

        // 主从同步校验
        LocalDateTime slaveUpdateTime =
                merchandiseReadMapper.selectUpdateTime(merchandiseCode);
        if (slaveUpdateTime == null) {
            if (retryCount < MAX_RETRY) {
                scheduleRetry(message, retryCount);
                return;
            }
            StructuredLog.warn(log)
                    .message("Slave data not found after max retry")
                    .put("merchandiseCode", merchandiseCode)
                    .put("retryCount", retryCount)
                    .log();
            return;
        }

        if (slaveUpdateTime.isBefore(message.getUpdateTime())) {
            if (retryCount < MAX_RETRY) {
                scheduleRetry(message, retryCount);
                return;
            }
            StructuredLog.error(log)
                    .message("Master-slave sync timeout")
                    .put("merchandiseCode", merchandiseCode)
                    .put("messageUpdateTime", message.getUpdateTime().toString())
                    .put("slaveUpdateTime", slaveUpdateTime.toString())
                    .log();
            // 超过最大重试次数，仍然执行刷新（用从库当前的数据兜底）
        }

        // 确定要刷新的维度
        Set<QueryOption> optionsToRefresh = resolveOptions(message);

        // 逐维度刷新Redis
        for (QueryOption option : optionsToRefresh) {
            refreshDimension(merchandiseCode, option);
        }
    }

    private void refreshDimension(String merchandiseCode, QueryOption option) {
        try {
            DataLoader<?> loader = loaderMap.get(option);
            if (loader == null)
                return;

            // 从数据库加载最新数据
            Object freshData = loader.loadFromDb(merchandiseCode);

            // 刷新Redis
            if (freshData != null)
                redisCacheService.put(option, merchandiseCode, freshData);
            else
                redisCacheService.evict(option, merchandiseCode);
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("Refresh dimension failed")
                    .put("merchandiseCode", merchandiseCode)
                    .put("option", option.name())
                    .exception(e)
                    .log();
        }
    }

    private Set<QueryOption> resolveOptions(MerchandiseChangeMessage message) {
        List<String> changed = message.getChangedOptions();
        if (changed == null || changed.isEmpty()) {
            return EnumSet.of(QueryOption.BASE, QueryOption.PRICE,
                    QueryOption.SKU, QueryOption.BRAND,
                    QueryOption.CATEGORY, QueryOption.IMAGE,
                    QueryOption.ATTRIBUTE, QueryOption.DESCRIPTION);
        }
        Set<QueryOption> options = EnumSet.noneOf(QueryOption.class);
        for (String name : changed) {
            try {
                options.add(QueryOption.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException ignored) {

            }
        }
        return options;
    }

    private void scheduleRetry(MerchandiseChangeMessage message, int retryCount) {
        long delay = RETRY_DELAYS[Math.min(retryCount, RETRY_DELAYS.length - 1)];
        retryScheduler.schedule(
                () -> executeWithRetry(message, retryCount + 1),
                delay, TimeUnit.MILLISECONDS);
    }
}
