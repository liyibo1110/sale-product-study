package com.github.liyibo1110.api.cache;

import com.github.liyibo1110.api.enums.QueryOption;

import java.util.List;
import java.util.stream.Collectors;

/**
 * redis的key结构化名称生成器。
 * @author liyibo
 * @date 2026-07-23 13:57
 */
public final class RedisCacheKey {

    private static final String PREFIX = "merchandise";

    private RedisCacheKey() {}

    public static String buildKey(QueryOption option, String merchandiseCode) {
        return PREFIX + ":" + option.name().toLowerCase() + ":" + merchandiseCode;
    }

    public static List<String> buildKeys(QueryOption option,
                                         List<String> merchandiseCodes) {
        return merchandiseCodes.stream()
                .map(code -> buildKey(option, code))
                .collect(Collectors.toList());
    }
}
