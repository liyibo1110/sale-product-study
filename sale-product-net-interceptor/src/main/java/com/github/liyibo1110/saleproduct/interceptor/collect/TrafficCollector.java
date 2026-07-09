package com.github.liyibo1110.saleproduct.interceptor.collect;

import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import com.github.liyibo1110.saleproduct.base.trace.TraceContext;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 流量采集器，负责收集请求的uri、转换后的Java格式入参，php接口的响应码和响应体。
 * 通过MQ发送给质检服务，服务只需要Java格式入参和php接口的响应，用来和Java响应做比对，php格式原始入参不需要。
 * 参数转换的工作在此interceptor服务里就完成了，质检服务不关心参数转换的具体逻辑。
 * 因为是异步的，所以采集失败也不会影响这个转发服务的正常流程。
 * @author liyibo
 * @date 2026-07-08 12:58
 */
@Component
public class TrafficCollector {

    private static final Logger log = LoggerFactory.getLogger(TrafficCollector.class);

    private static final String TOPIC = "traffic-collect-topic";

    private final RocketMQTemplate rocketMQTemplate;

    public TrafficCollector(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    public void collectAsync(String uri, byte[] javaRequestBody, byte[] responseBody, int responseStatus) {
        TrafficRecord record = new TrafficRecord();
        record.setUri(uri);
        record.setJavaRequestBody(javaRequestBody); // Java接口对应的请求体（即参数）
        record.setResponseBody(responseBody);   // php接口的响应体
        record.setResponseStatus(responseStatus);   // php接口的响应码
        record.setCollectTime(System.currentTimeMillis());
        record.setTraceId(TraceContext.getTraceId());

        // 异步发送，不阻塞当前请求
        rocketMQTemplate.asyncSend(TOPIC, record, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                // 发送成功，不需要额外处理
            }

            @Override
            public void onException(Throwable e) {
                // 采集失败不影响生产流量，只记日志
                StructuredLog.warn(log)
                        .message("流量采集消息发送失败")
                        .put("uri", uri)
                        .exception(e)
                        .log();
            }
        });
    }
}
