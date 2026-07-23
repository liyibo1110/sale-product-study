package com.github.liyibo1110.api.cache;

/**
 * 空值标记，用一个固定的字节序列表示"数据库里查不到"
 * @author liyibo
 * @date 2026-07-22 15:49
 */
public class NullValueMarker {

    // 4个字节的固定标记
    private static final byte[] NULL_MARKER = new byte[] { 0x00, 0x00, 0x00, 0x01 };

    // 空值缓存的TTL：2分钟
    public static final long NULL_TTL_SECONDS = 120;

    public static byte[] marker() {
        return NULL_MARKER;
    }

    public static boolean isNullMarker(byte[] bytes) {
        if (bytes == null || bytes.length != NULL_MARKER.length)
            return false;

        return bytes[0] == NULL_MARKER[0]
                && bytes[1] == NULL_MARKER[1]
                && bytes[2] == NULL_MARKER[2]
                && bytes[3] == NULL_MARKER[3];
    }
}
