package com.github.liyibo1110.saleproduct.check.interfacecheck;

import lombok.Data;

import java.io.Serializable;

/**
 * 这个就是net-interceptor通过MQ发来的质检服务消息体
 * @author liyibo
 * @date 2026-07-16 14:49
 */
@Data
public class TrafficRecord implements Serializable {

    private String uri;
    private byte[] javaRequestBody;
    private byte[] responseBody;
    private int responseStatus;
    private long collectTime;
}
