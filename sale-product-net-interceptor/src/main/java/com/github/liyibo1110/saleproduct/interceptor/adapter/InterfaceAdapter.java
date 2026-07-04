package com.github.liyibo1110.saleproduct.interceptor.adapter;

/**
 * php接口 -> Java接口的适配器。
 * @author liyibo
 * @date 2026-07-03 16:04
 */
public interface InterfaceAdapter {

    /**
     * 该适配器负责的URI。
     */
    String supportedUri();

    /**
     * 将PHP格式的请求参数转换为Java格式。
     */
    byte[] convertRequest(byte[] phpRequestBody);

    /**
     * 将Java格式的响应转换回    PHP格式。
     */
    byte[] convertResponse(byte[] javaResponseBody);
}
