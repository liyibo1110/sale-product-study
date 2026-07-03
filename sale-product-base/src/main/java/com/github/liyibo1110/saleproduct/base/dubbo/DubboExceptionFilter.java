package com.github.liyibo1110.saleproduct.base.dubbo;

import com.github.liyibo1110.saleproduct.base.exception.BizException;
import com.github.liyibo1110.saleproduct.base.exception.ValidationException;
import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

/**
 * @author liyibo
 * @date 2026-07-02 11:01
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class DubboExceptionFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result = invoker.invoke(invocation);

        if (result.hasException()) {
            Throwable exception = result.getException();

            if (exception instanceof BizException || exception instanceof ValidationException)
                return result;

            StructuredLog.error(log)
                    .message("Dubbo provider exception")
                    .put("service", invoker.getInterface().getName())
                    .put("method", invocation.getMethodName())
                    .exception(exception)
                    .log();
        }

        return result;
    }
}
