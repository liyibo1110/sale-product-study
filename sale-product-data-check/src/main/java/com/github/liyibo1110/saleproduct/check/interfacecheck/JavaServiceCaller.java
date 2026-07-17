package com.github.liyibo1110.saleproduct.check.interfacecheck;

import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Java商品读服务的调用器。
 * @author liyibo
 * @date 2026-07-16 14:31
 */
@Component
public class JavaServiceCaller {

    private static final Logger log = LoggerFactory.getLogger(JavaServiceCaller.class);

    private final OkHttpClient httpClient;
    private final InterfaceCheckConfig config;

    public JavaServiceCaller(InterfaceCheckConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    public byte[] call(String uri, byte[] javaRequestBody) {
        String url = config.getJavaServiceBaseUrl() + uri;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        // 如果有请求体，根据URI判断是GET带query参数还是POST带body
        if (javaRequestBody != null && javaRequestBody.length > 0)
            request = buildRequestWithBody(url, uri, javaRequestBody);

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() != 200) {
                StructuredLog.warn(log)
                        .message("Java服务返回非200")
                        .put("uri", uri)
                        .put("status", response.code())
                        .log();
                return null;
            }
            return response.body() != null
                    ? response.body().bytes()
                    : null;
        } catch (IOException e) {
            StructuredLog.warn(log)
                    .message("调用Java服务失败")
                    .put("uri", uri)
                    .exception(e)
                    .log();
            return null;
        }
    }

    private Request buildRequestWithBody(String baseUrl, String uri, byte[] body) {
        // 商品读接口都是GET请求，参数在query string里，javaRequestBody存的就是query参数的字节表示
        String queryString = new String(body, StandardCharsets.UTF_8);
        String fullUrl = config.getJavaServiceBaseUrl() + uri + "?" + queryString;
        return new Request.Builder()
                .url(fullUrl)
                .get()
                .build();
    }
}
