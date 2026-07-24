package com.github.liyibo1110.api.cache.spring;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.caffinitas.ohc.OHCache;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;

/**
 * 基于Spring Cache的Cache接口实现封装，内部实现基于OHC。
 * @author liyibo
 * @date 2026-07-23 14:35
 */
public class OhcCache implements Cache {

    private final String name;
    private final OHCache<String, byte[]> ohCache;

    private static final ThreadLocal<Kryo> KRYO_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(false);
        return kryo;
    });

    public OhcCache(String name, OHCache<String, byte[]> ohCache) {
        this.name = name;
        this.ohCache = ohCache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return ohCache;
    }

    @Override
    public ValueWrapper get(Object key) {
        byte[] bytes = ohCache.get(key.toString());
        if (bytes == null)
            return null;

        Object value = deserialize(bytes);
        return new SimpleValueWrapper(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        byte[] bytes = ohCache.get(key.toString());
        if (bytes == null)
            return null;

        return (T) deserialize(bytes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        byte[] bytes = ohCache.get(key.toString());
        if (bytes != null)
            return (T) deserialize(bytes);

        try {
            T value = valueLoader.call();
            if (value != null)
                put(key, value);

            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        if (value == null)
            return;

        byte[] bytes = serialize(value);
        ohCache.put(key.toString(), bytes);
    }

    @Override
    public void evict(Object key) {
        ohCache.remove(key.toString());
    }

    @Override
    public void clear() {
        // OHC没有clear方法，生产环境中几乎不会调用
    }

    private byte[] serialize(Object value) {
        Kryo kryo = KRYO_LOCAL.get();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        Output output = new Output(baos);
        kryo.writeClassAndObject(output, value);
        output.flush();
        return baos.toByteArray();
    }

    private Object deserialize(byte[] bytes) {
        Kryo kryo = KRYO_LOCAL.get();
        Input input = new Input(bytes);
        return kryo.readClassAndObject(input);
    }
}
