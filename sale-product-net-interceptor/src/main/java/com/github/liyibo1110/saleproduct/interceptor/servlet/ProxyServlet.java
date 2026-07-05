package com.github.liyibo1110.saleproduct.interceptor.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyContext;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyFilter;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyFilterChain;
import com.github.liyibo1110.saleproduct.interceptor.exception.ProxyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author liyibo
 * @date 2026-07-04 19:24
 */
public class ProxyServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ProxyServlet.class);

    /** 转发时要去掉的header */
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection", "keep-alive", "transfer-encoding",
            "te", "trailer", "upgrade",
            "proxy-authorization", "proxy-authenticate"
    );

    private final List<ProxyFilter> filters;
    private final ObjectMapper objectMapper;

    public ProxyServlet(List<ProxyFilter> filters, ObjectMapper objectMapper) {
        this.filters = filters;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        long startTime = System.currentTimeMillis();

        try {
            // 读取原始请求数据
            String queryString = req.getQueryString();
            String method = req.getMethod();
            byte[] body = StreamUtils.copyToByteArray(req.getInputStream());
            Map<String, String> headers = extractHeaders(req);

            // 构建上下文，启动过滤器链
            ProxyContext context = new ProxyContext(uri, queryString, method, body, headers);
            ProxyFilterChain chain = new ProxyFilterChain(filters);
            chain.doFilter(context);

            // 将响应写回给调用方
            writeResponse(resp, context);
        } catch (ProxyException e) {
            StructuredLog.warn(log)
                    .message("代理处理异常")
                    .put("uri", uri)
                    .put("httpStatus", e.getHttpStatus())
                    .put("errorCode", e.getErrorCode())
                    .exception(e)
                    .log();
            writeErrorResponse(resp, e.getHttpStatus(), e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("代理处理未知异常")
                    .put("uri", uri)
                    .exception(e)
                    .log();
            writeErrorResponse(resp, 502, "bad_gateway", "代理内部错误");
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            StructuredLog.info(log)
                    .message("请求处理完成")
                    .put("uri", uri)
                    .put("elapsed", elapsed)
                    .log();
        }
    }

    private void writeResponse(HttpServletResponse resp, ProxyContext context) throws IOException {
        resp.setStatus(context.getResponseStatusCode());
        if (context.getResponseHeaders() != null) {
            context.getResponseHeaders().forEach(resp::setHeader);
        }
        if (context.getResponseBody() != null) {
            resp.getOutputStream().write(context.getResponseBody());
            resp.getOutputStream().flush();
        }
    }

    private void writeErrorResponse(HttpServletResponse resp, int httpStatus,
                                    String errorCode, String message) throws IOException {
        resp.setStatus(httpStatus);
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("code", errorCode);
        errorBody.put("message", message);
        resp.getOutputStream().write(objectMapper.writeValueAsBytes(errorBody));
        resp.getOutputStream().flush();
    }

    /**
     * 提取header，但是会跳过逐跳header，它们不应该被代理层转发。
     */
    private Map<String, String> extractHeaders(HttpServletRequest req) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (HOP_BY_HOP_HEADERS.contains(name.toLowerCase()))
                continue;
            headers.put(name, req.getHeader(name));
        }
        return headers;
    }
}
