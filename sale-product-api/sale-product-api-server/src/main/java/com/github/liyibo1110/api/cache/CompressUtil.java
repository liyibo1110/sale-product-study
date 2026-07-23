package com.github.liyibo1110.api.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author liyibo
 * @date 2026-07-22 15:35
 */
public final class CompressUtil {

    /** 小于这个阈值不压缩 */
    private static final int COMPRESS_THRESHOLD = 1024;

    private CompressUtil() {}

    public static byte[] compress(byte[] data) {
        if (data == null || data.length < COMPRESS_THRESHOLD)
            return data;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
                gzip.write(data);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            return data;
        }
    }

    public static byte[] decompress(byte[] data) {
        if (data == null || data.length == 0)
            return data;

        // 检查GZIP魔数判断是否压缩过
        if (data.length < 2 || data[0] != (byte) 0x1f || data[1] != (byte) 0x8b)
            return data;

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            try (GZIPInputStream gzip = new GZIPInputStream(bais)) {
                return gzip.readAllBytes();
            }
        } catch (IOException e) {
            return data;
        }
    }
}
