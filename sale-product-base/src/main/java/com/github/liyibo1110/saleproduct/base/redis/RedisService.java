package com.github.liyibo1110.saleproduct.base.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis访问器。
 * @author liyibo
 * @date 2026-07-02 11:31
 */
@Component
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        return stringRedisTemplate.delete(key);
    }

    public Long delete(Collection<String> keys) {
        return stringRedisTemplate.delete(keys);
    }

    public Boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    public Long decrement(String key) {
        return stringRedisTemplate.opsForValue().decrement(key);
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    public Long getExpire(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    public Boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
    }

    public void hPut(String key, String hashKey, String value) {
        stringRedisTemplate.opsForHash().put(key, hashKey, value);
    }

    public void hPutAll(String key, Map<String, String> map) {
        stringRedisTemplate.opsForHash().putAll(key, map);
    }

    public Object hGet(String key, String hashKey) {
        return stringRedisTemplate.opsForHash().get(key, hashKey);
    }

    public Map<Object, Object> hGetAll(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    public Long hDelete(String key, Object... hashKeys) {
        return stringRedisTemplate.opsForHash().delete(key, hashKeys);
    }

    public Boolean hHasKey(String key, String hashKey) {
        return stringRedisTemplate.opsForHash().hasKey(key, hashKey);
    }

    public Long hIncrement(String key, String hashKey, long delta) {
        return stringRedisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    public Long sAdd(String key, String... values) {
        return stringRedisTemplate.opsForSet().add(key, values);
    }

    public Set<String> sMembers(String key) {
        return stringRedisTemplate.opsForSet().members(key);
    }

    public Boolean sIsMember(String key, String value) {
        return stringRedisTemplate.opsForSet().isMember(key, value);
    }

    public Long sRemove(String key, Object... values) {
        return stringRedisTemplate.opsForSet().remove(key, values);
    }

    public Long sSize(String key) {
        return stringRedisTemplate.opsForSet().size(key);
    }

    public Long lPush(String key, String value) {
        return stringRedisTemplate.opsForList().leftPush(key, value);
    }

    public Long lRightPush(String key, String value) {
        return stringRedisTemplate.opsForList().rightPush(key, value);
    }

    public Long lPushAll(String key, String... values) {
        return stringRedisTemplate.opsForList().leftPushAll(key, values);
    }

    public List<String> lRange(String key, long start, long end) {
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    public String lPop(String key) {
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    public String rPop(String key) {
        return stringRedisTemplate.opsForList().rightPop(key);
    }

    public Long lSize(String key) {
        return stringRedisTemplate.opsForList().size(key);
    }

    public void lTrim(String key, long start, long end) {
        stringRedisTemplate.opsForList().trim(key, start, end);
    }

    public void deleteByPattern(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty())
            stringRedisTemplate.delete(keys);
    }

    @SuppressWarnings("unchecked")
    public <T> T executeScript(DefaultRedisScript<T> script, List<String> keys, Object... args) {
        return stringRedisTemplate.execute(script, keys, args);
    }

    public <T> T executeScript(DefaultRedisScript<T> script, String key, Object... args) {
        return executeScript(script, Collections.singletonList(key), args);
    }
}
