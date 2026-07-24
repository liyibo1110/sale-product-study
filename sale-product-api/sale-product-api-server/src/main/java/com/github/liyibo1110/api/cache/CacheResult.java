package com.github.liyibo1110.api.cache;

/**
 * 缓存查询结果包装类，区分三种状态：
 * 1、miss：Key在Redis中不存在（缓存未命中）
 * 2、empty：Key存在但数据为空（命中空值标记，表示数据源里查不到）
 * 3、hit：Key存在且数据正常
 * @author liyibo
 * @date 2026-07-23 14:12
 */
public class CacheResult<T> {

    private final T data;
    private final boolean exists;
    private final long remainTtlSeconds;

    private CacheResult(T data, boolean exists, long remainTtlSeconds) {
        this.data = data;
        this.exists = exists;
        this.remainTtlSeconds = remainTtlSeconds;
    }

    /**
     * 缓存miss，Key在Redis中不存在
     */
    public static <T> CacheResult<T> miss() {
        return new CacheResult<>(null, false, -1);
    }

    /**
     * 缓存命中，但数据为空（空值标记）
     */
    public static <T> CacheResult<T> empty() {
        return new CacheResult<>(null, true, -1);
    }

    /**
     * 缓存命中，数据正常
     */
    public static <T> CacheResult<T> hit(T data) {
        return new CacheResult<>(data, true, -1);
    }

    /**
     * 缓存命中，数据正常，附带剩余TTL
     */
    public static <T> CacheResult<T> hit(T data, long remainTtlSeconds) {
        return new CacheResult<>(data, true, remainTtlSeconds);
    }

    public T getData() {
        return data;
    }

    /**
     * 数据是否存在（包括空值标记也算存在）
     */
    public boolean isExists() {
        return exists;
    }

    public boolean isMiss() {
        return !exists;
    }

    public long getRemainTtlSeconds() {
        return remainTtlSeconds;
    }
}
