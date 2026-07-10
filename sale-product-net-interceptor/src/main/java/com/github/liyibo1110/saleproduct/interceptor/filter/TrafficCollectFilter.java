package com.github.liyibo1110.saleproduct.interceptor.filter;

import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import com.github.liyibo1110.saleproduct.interceptor.adapter.AdapterRegistry;
import com.github.liyibo1110.saleproduct.interceptor.adapter.InterfaceAdapter;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyContext;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyFilter;
import com.github.liyibo1110.saleproduct.interceptor.chain.ProxyFilterChain;
import com.github.liyibo1110.saleproduct.interceptor.collect.TrafficCollector;
import com.github.liyibo1110.saleproduct.interceptor.route.RouteTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 职责链第五层：流量采集。
 * 如果走的是php接口，并且开启了采集开关，这里会把转换后的Java入参和php接口的实际响应，异步发送给质检服务。
 * @author liyibo
 * @date 2026-07-09 14:47
 */
@Component
public class TrafficCollectFilter implements ProxyFilter {

    private static final Logger log = LoggerFactory.getLogger(TrafficCollectFilter.class);

    private final AdapterRegistry adapterRegistry;
    private final TrafficCollector trafficCollector;

    public TrafficCollectFilter(AdapterRegistry adapterRegistry,
                                TrafficCollector trafficCollector) {
        this.adapterRegistry = adapterRegistry;
        this.trafficCollector = trafficCollector;
    }

    @Override
    public void doFilter(ProxyContext context, ProxyFilterChain chain) throws Exception {
        try {
            // 只有走的php接口，并且开启了质检开关，才会发消息给质检服务
            if (context.getRouteTarget() == RouteTarget.PHP && context.isCollectEnabled())
                collect(context);
        } catch (Exception e) {
            // 采集发生的任何失败不能影响生产流量，只记日志
            StructuredLog.warn(log)
                    .message("流量采集异常")
                    .put("uri", context.getUri())
                    .exception(e)
                    .log();
        }
        chain.doFilter(context);
    }

    /**
     * 发送消息给质检服务。
     */
    private void collect(ProxyContext context) {
        InterfaceAdapter adapter = adapterRegistry.getAdapter(context.getUri());
        if (adapter == null) {
            StructuredLog.warn(log)
                    .message("流量采集跳过，未找到适配器")
                    .put("uri", context.getUri())
                    .log();
            return;
        }
        // 转换参数，生成Java格式入参
        byte[] javaRequestBody = adapter.convertRequest(context.getOriginalBody());
        // 发送异步消息
        trafficCollector.collectAsync(context.getUri(), javaRequestBody, context.getResponseBody(), context.getResponseStatusCode());
    }

    @Override
    public int getOrder() {
        return 500;
    }
}
