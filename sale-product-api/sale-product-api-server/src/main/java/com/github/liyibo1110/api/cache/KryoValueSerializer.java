package com.github.liyibo1110.api.cache;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.caffinitas.ohc.CacheSerializer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * OHC的byte[] value序列化器。
 * @author liyibo
 * @date 2026-07-22 15:51
 */
public class KryoValueSerializer<T> implements CacheSerializer<T> {

    private final Class<T> type;

    // Kryo不是线程安全的，用ThreadLocal隔离
    private static final ThreadLocal<Kryo> KRYO_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(false);
        return kryo;
    });

    public KryoValueSerializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public void serialize(T value, ByteBuffer buf) {
        Kryo kryo = KRYO_LOCAL.get();
        // 先序列化到byte[]再写入ByteBuffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        Output output = new Output(baos);
        kryo.writeObject(output, value);
        output.flush();
        byte[] bytes = baos.toByteArray();
        buf.putInt(bytes.length);
        buf.put(bytes);
    }

    @Override
    public T deserialize(ByteBuffer buf) {
        Kryo kryo = KRYO_LOCAL.get();
        int length = buf.getInt();
        byte[] bytes = new byte[length];
        buf.get(bytes);
        Input input = new Input(bytes);
        return kryo.readObject(input, type);
    }

    @Override
    public int serializedSize(T value) {
        Kryo kryo = KRYO_LOCAL.get();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        Output output = new Output(baos);
        kryo.writeObject(output, value);
        output.flush();
        // 4字节长度前缀 + 序列化内容
        return 4 + baos.size();
    }
}
