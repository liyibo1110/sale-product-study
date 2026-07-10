package com.github.liyibo1110.saleproduct.interceptor.filter;

import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyContext;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyFilter;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyFilterChain;
import com.github.liyibo1110.saleproduct.interceptor.exception.ProxyException;
import com.github.liyibo1110.saleproduct.interceptor.forward.ForwardResponse;
import com.github.liyibo1110.saleproduct.interceptor.forward.HttpForwarder;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;

/**
 * 职责链第三层：请求转发。
 * 向真正的后端转发请求，并获取响应。
 * @author liyibo
 * @date 2026-07-09 14:41
 */
@Component
public class ForwardFilter implements ProxyFilter {

    private final HttpForwarder httpForwarder;

    public ForwardFilter(HttpForwarder httpForwarder) {
        this.httpForwarder = httpForwarder;
    }

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) throws Exception {
        String targetUrl = buildTargetUrl(context);

        try {
            ForwardResponse response = httpForwarder.forward(context.getMethod(), targetUrl, context.getHeaders(), context.getForwardBody());
            context.setResponseStatusCode(response.getStatusCode());
            context.setResponseHeaders(response.getResponseHeaders());
            context.setResponseBody(response.getBody());
        } catch (SocketTimeoutException e) {
            throw ProxyException.gatewayTimeout("转发超时: " + targetUrl, e);
        } catch (Exception e) {
            throw ProxyException.badGateway("转发失败: " + targetUrl, e);
        }
        chain.doFilter(context);
    }

    /**
     * 生成url
     */
    private String buildTargetUrl(ProxyContext context) {
        String url = context.getTargetBaseUrl() + context.getUri();
        if (context.getQueryString() != null)
            url = url + "?" + context.getQueryString();
        return url;
    }

    @Override
    public int getOrder() {
        return 300;
    }
}
