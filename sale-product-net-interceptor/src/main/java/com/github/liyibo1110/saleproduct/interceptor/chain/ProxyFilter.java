package com.github.liyibo1110.saleproduct.interceptor.chain;

/**
 * 转发职责链中每个Filter的公共接口。
 * @author liyibo
 * @date 2026-07-03 16:01
 */
public interface ProxyFilter {

    /**
     * 执行特定的Filter动作。
     */
    void doFilter(ProxyContext context, ProxyFilterChain chain) throws Exception;

    /**
     * 返回该Filter的执行顺序。
     */
    int getOrder();
}
