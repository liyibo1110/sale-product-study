package com.github.liyibo1110.saleproduct.base.id;

import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * 分布式ID生成器（Snowflake算法）
 * @author liyibo
 * @date 2026-07-02 10:41
 */
@Slf4j
public class IdGenerator {

    /** 起始时间戳 (2024-01-01 00:00:00) */
    private static final long EPOCH = 1704067200000L;

    /** 机器 ID 占 10 位 */
    private static final long WORKER_ID_BITS = 10L;

    /** 序列号占 12 位 */
    private static final long SEQUENCE_BITS = 12L;

    /** 机器 ID 左移 12 位 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /** 时间戳左移 22 位 (10 + 12) */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /** 序列号掩码 4095 */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /** 机器 ID 掩码 1023 */
    private static final long WORKER_MASK = ~(-1L << WORKER_ID_BITS);

    private static long workerId;
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    static {
        String workerMark = null;
        try {
            workerMark = InetAddress.getLocalHost().getHostAddress();
            workerId = workerMark.hashCode() & WORKER_MASK;
        } catch (UnknownHostException e) {
            StructuredLog.error(log)
                    .message("获取本机IP失败")
                    .exception(e)
                    .log();
            workerMark = System.currentTimeMillis() + UUID.randomUUID().toString();
            workerId = workerMark.hashCode() & WORKER_MASK;
        } finally {
            StructuredLog.info(log)
                    .message("工作节点初始化完成")
                    .put("workerMark", workerMark)
                    .put("workerId", workerId)
                    .log();
        }
    }

    private IdGenerator() {
        throw new UnsupportedOperationException();
    }

    /**
     * 生成纯数字雪花 ID
     */
    public static long nextId() {
        return generateId();
    }

    /**
     * 生成 UUID（去除中划线）
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static synchronized long generateId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp)
            throw new RuntimeException(String.format("时钟回拨，拒绝生成ID %d 毫秒", lastTimestamp - timestamp));

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0)
                timestamp = tilNextMillis(lastTimestamp);
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp)
            timestamp = System.currentTimeMillis();

        return timestamp;
    }
}
