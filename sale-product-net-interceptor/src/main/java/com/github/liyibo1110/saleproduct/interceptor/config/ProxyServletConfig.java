package com.github.liyibo1110.saleproduct.interceptor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyFilter;
import com.github.liyibo1110.saleproduct.interceptor.servlet.ProxyServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ServletRegistrationBean自动配置，作用是将ProxyServlet以高优先级注册到servlet容器中，并拦截所有请求。
 * @author liyibo
 * @date 2026-07-06 11:48
 */
@Configuration
public class ProxyServletConfig {

    @Bean
    public ServletRegistrationBean<ProxyServlet> proxyServlet(List<ProxyFilter> filters, ObjectMapper objectMapper) {
        // 按order排序，值越小越先执行，List<ProxyFilter>是自动注入的
        List<ProxyFilter> sortedFilters = filters.stream()
                .sorted(Comparator.comparingInt(ProxyFilter::getOrder))
                .collect(Collectors.toList());

        // 构建并注册ProxyServlet
        ProxyServlet servlet = new ProxyServlet(sortedFilters, objectMapper);
        ServletRegistrationBean<ProxyServlet> registration = new ServletRegistrationBean<>(servlet, "/*");
        // 设置最高优先级，确保在DispatcherServlet之前拦截请求
        registration.setLoadOnStartup(1);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
