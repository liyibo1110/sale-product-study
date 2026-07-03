package com.github.liyibo1110.saleproduct.base.trace;

import com.github.liyibo1110.saleproduct.base.constant.CommonConstants;
import com.github.liyibo1110.saleproduct.base.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * HTTP 请求上下文过滤器，统一管理traceId和userId的设置与清理。
 * @author liyibo
 * @date 2026-07-02 11:34
 */
@Slf4j
public class TraceFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                String traceId = httpRequest.getHeader(CommonConstants.HEADER_TRACE_ID);
                if (traceId == null || traceId.isBlank())
                    traceId = generateTraceId();

                TraceContext.setTraceId(traceId);
                MDC.put(CommonConstants.TRACE_ID_KEY, traceId);
                httpResponse.addHeader(CommonConstants.HEADER_TRACE_ID, traceId);

                String userIdStr = httpRequest.getHeader(CommonConstants.HEADER_USER_ID);
                if (userIdStr != null && !userIdStr.isBlank()) {
                    try {
                        UserContext.setUserId(Long.parseLong(userIdStr));
                    } catch (NumberFormatException ignored) {

                    }
                }
            }
            chain.doFilter(request, response);
        } finally {
            TraceContext.clear();
            UserContext.clear();
            MDC.remove(CommonConstants.TRACE_ID_KEY);
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
