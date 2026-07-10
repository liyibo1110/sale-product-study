package com.github.liyibo1110.saleproduct.interceptor.filter;

import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyContext;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyFilter;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyFilterChain;
import com.github.liyibo1110.saleproduct.interceptor.route.RouteDecision;
import com.github.liyibo1110.saleproduct.interceptor.route.RouteRuleManager;
import org.springframework.stereotype.Component;

/**
 * 职责链第一层：路由决策。
 * 负责从Nacos读取路由的规则，然后判断请求是转发php接口还是Java接口。
 * @author liyibo
 * @date 2026-07-09 13:56
 */
@Component
public class RouteFilter implements ProxyFilter {

    private final RouteRuleManager routeRuleManager;

    public RouteFilter(RouteRuleManager routeRuleManager) {
        this.routeRuleManager = routeRuleManager;
    }

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) throws Exception {
        RouteDecision decision = routeRuleManager.resolve(context.getUri());
        // 回写context转发相关字段
        context.setRouteTarget(decision.getTarget());
        context.setTargetBaseUrl(decision.getTargetBaseUrl());
        context.setCollectEnabled(decision.isCollectEnabled());
        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
