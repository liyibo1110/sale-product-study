package com.github.liyibo1110.api.loader;

import com.github.liyibo1110.api.enums.QueryOption;

/**
 * @author liyibo
 * @date 2026-07-23 11:50
 */
public interface DataLoader<T> {

    /**
     * 加载数据（三层缓存链路：OHC -> Redis -> DB）
     */
    T load(String merchandiseCode);

    /**
     * 直接从数据库加载，不经过缓存，供缓存刷新使用
     */
    T loadFromDb(String merchandiseCode);

    /**
     * 直接从本地缓存获取，不触发加载链路
     */
    default T getCached(String merchandiseCode) {
        return null;
    }

    /**
     * 对应的查询选项
     */
    QueryOption supportedOption();
}
