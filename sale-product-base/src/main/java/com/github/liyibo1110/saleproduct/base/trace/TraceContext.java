package com.github.liyibo1110.saleproduct.base.trace;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 链路追踪上下文，使用TransmittableThreadLocal支持线程池场景下的上下文透传。
 * @author liyibo
 * @date 2026-07-02 11:32
 */
public class TraceContext {

    private static final TransmittableThreadLocal<String> TRACE_ID_TL = new TransmittableThreadLocal<>();

    public static void setTraceId(String traceId) {
        TRACE_ID_TL.set(traceId);
    }

    public static String getTraceId() {
        return TRACE_ID_TL.get();
    }

    public static void clear() {
        TRACE_ID_TL.remove();
    }
}
