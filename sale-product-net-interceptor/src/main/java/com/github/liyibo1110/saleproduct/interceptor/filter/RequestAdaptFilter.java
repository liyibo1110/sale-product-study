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
 * 职责链第二层：入参转换。
 * 负责判断上一层是否要转发给Java，如果是，则通过内置对应的适配器，把php入参转换成Java格式。
 * 如果上一层是要转发给php，则不作额外处理。
 * @author liyibo
 * @date 2026-07-09 14:00
 */
@Component
public class RequestAdaptFilter implements ProxyFilter {

    private final AdapterRegistry adapterRegistry;

    public RequestAdaptFilter(AdapterRegistry adapterRegistry) {
        this.adapterRegistry = adapterRegistry;
    }

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) throws Exception {
        // 只处理要转发给Java接口的
        if (context.getRouteTarget() == RouteTarget.JAVA) {
            InterfaceAdapter adapter = adapterRegistry.getAdapter(context.getUri());
            if (adapter == null)
                throw ProxyException.internal("未找到URI对应的适配器: " + context.getUri());
            byte[] javaBody = adapter.convertRequest(context.getOriginalBody());
            context.setAdaptedRequestBody(javaBody);
        }
        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 200;
    }
}
