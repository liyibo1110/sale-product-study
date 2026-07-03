package com.github.liyibo1110.saleproduct.base.trace;

import com.github.liyibo1110.saleproduct.base.constant.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.MDC;

/**
 * Dubbo链路追踪过滤器。
 * 在Dubbo调用链中透传traceId，保证跨服务的日志可以通过同一个traceId关联。
 * Consumer端：从TraceContext读取traceId写入RpcContext attachment。
 * Provider端：从RpcContext attachment读取traceId写入TraceContext和MDC。
 * @author liyibo
 * @date 2026-07-02 11:33
 */
@Activate(group = {"consumer", "provider"})
public class TraceDubboFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String side = invoker.getUrl().getParameter("side", "provider");

        if ("consumer".equals(side)) {
            String traceId = TraceContext.getTraceId();
            if (traceId != null && !traceId.isBlank())
                RpcContext.getClientAttachment().setAttachment(CommonConstants.TRACE_ID_KEY, traceId);
        } else {
            String traceId = RpcContext.getServerAttachment().getAttachment(CommonConstants.TRACE_ID_KEY);
            if (traceId != null && !traceId.isBlank()) {
                TraceContext.setTraceId(traceId);
                MDC.put(CommonConstants.TRACE_ID_KEY, traceId);
            }
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            if ("provider".equals(side)) {
                TraceContext.clear();
                MDC.remove(CommonConstants.TRACE_ID_KEY);
            }
        }
    }
}
