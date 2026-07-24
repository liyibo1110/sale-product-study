package com.github.liyibo1110.api.cache.spring;

import org.caffinitas.ohc.CacheSerializer;

import java.nio.ByteBuffer;

/**
 * @author liyibo
 * @date 2026-07-23 14:40
 */
public class ByteArraySerializer implements CacheSerializer<byte[]> {

    @Override
    public void serialize(byte[] value, ByteBuffer buf) {
        buf.putInt(value.length);
        buf.put(value);
    }

    @Override
    public byte[] deserialize(ByteBuffer buf) {
        int length = buf.getInt();
        byte[] bytes = new byte[length];
        buf.get(bytes);
        return bytes;
    }

    @Override
    public int serializedSize(byte[] value) {
        return 4 + value.length;
    }
}
