package com.github.liyibo1110.saleproduct.check.interfacecheck;

import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 消费要质检的接口数据，并触发质检动作。
 * @author liyibo
 * @date 2026-07-17 11:35
 */
@Component
@RocketMQMessageListener(
        topic = "traffic-collect-topic",
        consumerGroup = "sale-product-checker-interface-verify"
)
public class InterfaceCheckConsumer implements RocketMQListener<TrafficRecord> {

    private static final Logger log = LoggerFactory.getLogger(InterfaceCheckConsumer.class);

    private final InterfaceCheckService interfaceCheckService;
    private final InterfaceCheckConfig config;

    public InterfaceCheckConsumer(InterfaceCheckService interfaceCheckService, InterfaceCheckConfig config) {
        this.interfaceCheckService = interfaceCheckService;
        this.config = config;
    }

    @Override
    public void onMessage(TrafficRecord record) {
        if (!config.isEnabled())
            return;

        // PHP返回非200的请求不做比对
        if (record.getResponseStatus() != 200)
            return;

        try {
            interfaceCheckService.verify(record);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("接口校验异常")
                    .put("uri", record.getUri())
                    .exception(e)
                    .log();
        }
    }
}
