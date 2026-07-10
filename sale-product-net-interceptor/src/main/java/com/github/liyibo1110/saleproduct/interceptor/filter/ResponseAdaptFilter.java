package com.github.liyibo1110.saleproduct.interceptor.filter;

import com.github.liyibo1110.saleproduct.interceptor.adapter.AdapterRegistry;
import com.github.liyibo1110.saleproduct.interceptor.adapter.InterfaceAdapter;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyContext;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyFilter;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyFilterChain;
import com.github.liyibo1110.saleproduct.interceptor.exception.ProxyException;
import com.github.liyibo1110.saleproduct.interceptor.route.RouteTarget;
import org.springframework.stereotype.Component;

/**
 * 职责链第四层：响应转换。
 * 如果走的是Java接口，则要把响应转换回php需要的响应格式。
 * @author liyibo
 * @date 2026-07-09 14:44
 */
@Component
public class ResponseAdaptFilter implements ProxyFilter {

    private final AdapterRegistry adapterRegistry;

    public ResponseAdaptFilter(AdapterRegistry adapterRegistry) {
        this.adapterRegistry = adapterRegistry;
    }

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) throws Exception {
        // 和RequestAdaptFilter一样，只处理要转发给Java接口的
        if (context.getRouteTarget() == RouteTarget.JAVA) {
            InterfaceAdapter adapter = adapterRegistry.getAdapter(context.getUri());
            if (adapter == null)
                throw ProxyException.internal("未找到URI对应的适配器: " + context.getUri());
            byte[] phpResponse = adapter.convertResponse(context.getResponseBody());
            context.setResponseBody(phpResponse);
        }
        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 400;
    }
}
