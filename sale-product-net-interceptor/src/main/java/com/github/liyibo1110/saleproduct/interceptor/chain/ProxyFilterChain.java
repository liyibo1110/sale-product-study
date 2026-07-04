package com.github.liyibo1110.saleproduct.interceptor.chain;

import java.util.List;

/**
 * 整条转发职责链。
 * @author liyibo
 * @date 2026-07-03 16:02
 */
public class ProxyFilterChain {

    private final List<ProxyFilter> filters;
    private int index = 0;

    public ProxyFilterChain(List<ProxyFilter> filters) {
        this.filters = filters;
    }

    /**
     * 启动整个转发链。
     */
    public void doFilter(ProxyContext context) throws Exception {
        if (index < filters.size()) {
            ProxyFilter filter = filters.get(index++);
            filter.doFilter(context, this);
        }
    }
}
