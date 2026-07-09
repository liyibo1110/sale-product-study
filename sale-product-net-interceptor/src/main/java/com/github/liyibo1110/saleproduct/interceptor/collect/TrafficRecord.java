package com.github.liyibo1110.saleproduct.interceptor.collect;

import lombok.Data;

/**
 * 发送给质检服务的MQ消息体。
 * @author liyibo
 * @date 2026-07-08 13:02
 */
@Data
public class TrafficRecord {

    private String uri;

    private byte[] javaRequestBody;

    private byte[] responseBody;

    private int responseStatus;

    private long collectTime;

    private String traceId;
}
