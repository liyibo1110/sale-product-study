package com.github.liyibo1110.saleproduct.interceptor.chain;

import com.github.liyibo1110.saleproduct.interceptor.route.RouteTarget;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 请求转发，再职责链上各节点传输的共享上下文，包含了请求信息和最终的响应。
 * @author liyibo
 * @date 2026-07-03 15:48
 */
@Getter
public class ProxyContext {

    // 构造时就设置，不可变
    private final String uri;
    private final String queryString;
    private final String method;
    private final byte[] originalBody;
    private final Map<String, String> headers;

    // 由RouteFilter后续写入
    @Setter
    private RouteTarget routeTarget;
    @Setter
    private String targetBaseUrl;
    @Setter
    private boolean collectEnabled;

    // 由RequestAdaptFilter写入（仅Java路径）
    @Setter
    private byte[] adaptedRequestBody;

    // 由ForwardFilter写入
    @Setter
    private int responseStatusCode;
    @Setter
    private Map<String, String> responseHeaders;
    @Setter
    private byte[] responseBody;

    public ProxyContext(String uri, String queryString, String method,
                        byte[] originalBody, Map<String, String> headers) {
        this.uri = uri;
        this.queryString = queryString;
        this.method = method;
        this.originalBody = originalBody;
        this.headers = headers;
    }

    /**
     * 获取实际转发用的请求体，Java路径下使用转换后的入参，PHP路径下使用原始请求体。
     */
    public byte[] getForwardBody() {
        return adaptedRequestBody != null ? adaptedRequestBody : originalBody;
    }
}
