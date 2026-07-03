package com.github.liyibo1110.saleproduct.base.lock;

import com.github.liyibo1110.saleproduct.base.exception.BizException;
import com.github.liyibo1110.saleproduct.base.exception.ErrorEnum;
import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务（编程式 API）。
 * @author liyibo
 * @date 2026-07-02 11:29
 */
@Slf4j
public class DistributedLockService {

    private final RedissonClient redissonClient;

    public DistributedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                StructuredLog.warn(log)
                        .message("获取分布式锁失败")
                        .put("lockKey", lockKey)
                        .log();
                throw BizException.of(ErrorEnum.FETCH_LOCK_FAILED);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw BizException.of(ErrorEnum.FETCH_LOCK_FAILED);
        } finally {
            if (acquired && lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }

    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable runnable) {
        executeWithLock(lockKey, waitTime, leaseTime, unit, () -> {
            runnable.run();
            return null;
        });
    }

    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, 3, 30, TimeUnit.SECONDS, supplier);
    }

    public void executeWithLock(String lockKey, Runnable runnable) {
        executeWithLock(lockKey, 3, 30, TimeUnit.SECONDS, runnable);
    }
}
