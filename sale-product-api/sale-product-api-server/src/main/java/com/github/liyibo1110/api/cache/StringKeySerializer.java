package com.github.liyibo1110.api.cache;

import org.caffinitas.ohc.CacheSerializer;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * OHC的String key序列化器。
 * @author liyibo
 * @date 2026-07-22 15:50
 */
public class StringKeySerializer implements CacheSerializer<String> {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @Override
    public void serialize(String value, ByteBuffer buf) {
        byte[] bytes = value.getBytes(UTF8);
        // 先写长度，再写内容
        buf.putInt(bytes.length);
        buf.put(bytes);
    }

    @Override
    public String deserialize(ByteBuffer buf) {
        int length = buf.getInt();
        byte[] bytes = new byte[length];
        buf.get(bytes);
        return new String(bytes, UTF8);
    }

    @Override
    public int serializedSize(String value) {
        // 4字节长度 + 实际内容
        return 4 + value.getBytes(UTF8).length;
    }
}
