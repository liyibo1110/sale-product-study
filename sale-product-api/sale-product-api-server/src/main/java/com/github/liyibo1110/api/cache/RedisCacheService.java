package com.github.liyibo1110.api.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.liyibo1110.api.enums.QueryOption;
import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author liyibo
 * @date 2026-07-23 13:58
 */
@Component
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final RedisTtlConfig ttlConfig;

    private static final int WARN_SIZE = 10 * 1024;
    private static final int ERROR_SIZE = 64 * 1024;

    private static final ThreadLocal<Kryo> KRYO_LOCAL =
            ThreadLocal.withInitial(() -> {
                Kryo kryo = new Kryo();
                kryo.setRegistrationRequired(false);
                kryo.setReferences(false);
                return kryo;
            });

    public RedisCacheService(RedisTemplate<String, byte[]> redisTemplate, RedisTtlConfig ttlConfig) {
        this.redisTemplate = redisTemplate;
        this.ttlConfig = ttlConfig;
    }

    /**
     * 从Redis获取单个维度的数据
     */
    public <T> T get(QueryOption option, String merchandiseCode, Class<T> type) {
        try {
            String key = RedisCacheKey.buildKey(option, merchandiseCode);
            byte[] bytes = redisTemplate.opsForValue().get(key);
            if (bytes == null || bytes.length == 0)
                return null;

            if (NullValueMarker.isNullMarker(bytes))
                return null;

            if (option == QueryOption.DESCRIPTION)
                bytes = CompressUtil.decompress(bytes);

            return deserialize(bytes, type);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("Redis get failed, fallback to null")
                    .put("option", option.name())
                    .put("merchandiseCode", merchandiseCode)
                    .exception(e)
                    .log();
            return null;
        }
    }

    /**
     * 批量获取同一维度的数据
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> batchGet(QueryOption option, List<String> merchandiseCodes, Class<T> type) {
        try {
            List<String> keys = RedisCacheKey.buildKeys(option, merchandiseCodes);
            List<byte[]> values = redisTemplate.opsForValue().multiGet(keys);
            if (values == null)
                return Collections.emptyMap();

            Map<String, T> result = new HashMap<>(merchandiseCodes.size());
            for (int i = 0; i < merchandiseCodes.size(); i++) {
                byte[] bytes = values.get(i);
                if (bytes != null && bytes.length > 0) {
                    // 跳过空值标记
                    if (NullValueMarker.isNullMarker(bytes))
                        continue;

                    if (option == QueryOption.DESCRIPTION)
                        bytes = CompressUtil.decompress(bytes);

                    T data = deserialize(bytes, type);
                    if (data != null)
                        result.put(merchandiseCodes.get(i), data);
                }
            }
            return result;
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("Redis batchGet failed, fallback to empty")
                    .put("option", option.name())
                    .put("size", merchandiseCodes.size())
                    .exception(e)
                    .log();
            return Collections.emptyMap();
        }
    }

    /**
     * 写入Redis
     */
    public <T> void put(QueryOption option, String merchandiseCode, T data) {
        if (data == null) {
            return;
        }
        try {
            String key = RedisCacheKey.buildKey(option, merchandiseCode);
            byte[] bytes = serialize(data);
            if (option == QueryOption.DESCRIPTION)
                bytes = CompressUtil.compress(bytes);

            checkValueSize(bytes, option, merchandiseCode);
            long ttlSeconds = calculateTtl(option);
            redisTemplate.opsForValue().set(key, bytes, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("Redis put failed")
                    .put("option", option.name())
                    .put("merchandiseCode", merchandiseCode)
                    .exception(e)
                    .log();
        }
    }

    /**
     * 删除Redis缓存
     */
    public void evict(QueryOption option, String merchandiseCode) {
        try {
            String key = RedisCacheKey.buildKey(option, merchandiseCode);
            redisTemplate.delete(key);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("Redis evict failed")
                    .put("option", option.name())
                    .put("merchandiseCode", merchandiseCode)
                    .exception(e)
                    .log();
        }
    }

    /**
     * 删除一个商品所有维度的缓存
     */
    public void evictAll(String merchandiseCode) {
        try {
            List<String> keys = Arrays.stream(QueryOption.values())
                    .filter(opt -> opt != QueryOption.ALL)
                    .map(opt -> RedisCacheKey.buildKey(opt, merchandiseCode))
                    .collect(Collectors.toList());
            redisTemplate.delete(keys);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("Redis evictAll failed")
                    .put("merchandiseCode", merchandiseCode)
                    .exception(e)
                    .log();
        }
    }

    private long calculateTtl(QueryOption option) {
        long baseTtl = ttlConfig.getTtlSeconds(option);
        // 在基础TTL的基础上加减20%的随机偏移
        long offset = (long) (baseTtl * 0.2 * (Math.random() - 0.5));
        return baseTtl + offset;
    }

    private void checkValueSize(byte[] bytes, QueryOption option, String merchandiseCode) {
        if (bytes.length > ERROR_SIZE) {
            StructuredLog.error(log)
                    .message("Redis value size exceeds error threshold")
                    .put("option", option.name())
                    .put("merchandiseCode", merchandiseCode)
                    .put("size", bytes.length)
                    .log();
        } else if (bytes.length > WARN_SIZE) {
            StructuredLog.warn(log)
                    .message("Redis value size exceeds warn threshold")
                    .put("option", option.name())
                    .put("merchandiseCode", merchandiseCode)
                    .put("size", bytes.length)
                    .log();
        }
    }

    private <T> byte[] serialize(T data) {
        Kryo kryo = KRYO_LOCAL.get();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        output.flush();
        return baos.toByteArray();
    }

    private <T> T deserialize(byte[] bytes, Class<T> type) {
        Kryo kryo = KRYO_LOCAL.get();
        Input input = new Input(bytes);
        return kryo.readObject(input, type);
    }

    /**
     * 从Redis获取数据，区分miss和空值标记
     */
    public <T> CacheResult<T> getWithNullCheck(QueryOption option,
                                               String merchandiseCode,
                                               Class<T> type) {
        try {
            String key = RedisCacheKey.buildKey(option, merchandiseCode);
            byte[] bytes = redisTemplate.opsForValue().get(key);
            if (bytes == null || bytes.length == 0)
                return CacheResult.miss();

            if (NullValueMarker.isNullMarker(bytes))
                return CacheResult.empty();

            if (option == QueryOption.DESCRIPTION)
                bytes = CompressUtil.decompress(bytes);

            T data = deserialize(bytes, type);
            return CacheResult.hit(data);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("Redis getWithNullCheck failed")
                    .put("option", option.name())
                    .put("merchandiseCode", merchandiseCode)
                    .exception(e)
                    .log();
            return CacheResult.miss();
        }
    }

    /**
     * 从Redis获取数据，附带剩余TTL（用于主动刷新判断）
     */
    public <T> CacheResult<T> getWithTtlCheck(QueryOption option,
                                              String merchandiseCode,
                                              Class<T> type) {
        try {
            String key = RedisCacheKey.buildKey(option, merchandiseCode);
            byte[] bytes = redisTemplate.opsForValue().get(key);
            if (bytes == null || bytes.length == 0)
                return CacheResult.miss();

            if (NullValueMarker.isNullMarker(bytes))
                return CacheResult.empty();

            // 顺带获取剩余TTL
            Long remainTtl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (option == QueryOption.DESCRIPTION)
                bytes = CompressUtil.decompress(bytes);

            T data = deserialize(bytes, type);
            return CacheResult.hit(data, remainTtl != null ? remainTtl : -1);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("Redis getWithTtlCheck failed")
                    .put("option", option.name())
                    .put("merchandiseCode", merchandiseCode)
                    .exception(e)
                    .log();
            return CacheResult.miss();
        }
    }

    /**
     * 存入空值标记
     */
    public void putNull(QueryOption option, String merchandiseCode) {
        try {
            String key = RedisCacheKey.buildKey(option, merchandiseCode);
            redisTemplate.opsForValue().set(key, NullValueMarker.marker(), NullValueMarker.NULL_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("Redis putNull failed")
                    .put("option", option.name())
                    .put("merchandiseCode", merchandiseCode)
                    .exception(e)
                    .log();
        }
    }
}
