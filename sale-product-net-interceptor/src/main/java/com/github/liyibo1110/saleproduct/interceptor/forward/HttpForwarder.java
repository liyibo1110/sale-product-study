package com.github.liyibo1110.saleproduct.interceptor.forward;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 负责发起HTTP请求，并将响应结果以统一的格式返回。
 * @author liyibo
 * @date 2026-07-06 13:18
 */
@Component
public class HttpForwarder {

    private final OkHttpClient httpClient;

    public HttpForwarder(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ForwardResponse forward(String method, String url, Map<String, String> headers, byte[] body)
            throws IOException {
        Headers.Builder headerBuilder = new Headers.Builder();
        headers.forEach(headerBuilder::add);

        RequestBody requestBody = resolveBody(method, headers, body);

        Request request = new Request.Builder()
                .url(url)
                .method(method, requestBody)
                .headers(headerBuilder.build())
                .build();

        // 发送请求并接收响应
        try (Response response = httpClient.newCall(request).execute()) {
            int statusCode = response.code();
            byte[] responseBody = response.body() != null
                    ? response.body().bytes()
                    : new byte[0];
            Map<String, String> responseHeaders = new LinkedHashMap<>();
            for (String name : response.headers().names())
                responseHeaders.put(name, response.header(name));
            return new ForwardResponse(statusCode, responseHeaders, responseBody);
        }
    }

    /**
     * 生成okhttp的RequestBody对象。
     */
    private RequestBody resolveBody(String method, Map<String, String> headers, byte[] body) {
        // 如果method是GET和HEAD，则直接返回，不携带请求体
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method))
            return null;

        String contentType = headers.getOrDefault("content-type", "application/octet-stream");
        byte[] content = (body != null) ? body : new byte[0];
        return RequestBody.create(content, MediaType.parse(contentType));
    }
}
