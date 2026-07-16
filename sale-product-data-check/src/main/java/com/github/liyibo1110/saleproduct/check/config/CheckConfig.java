package com.github.liyibo1110.saleproduct.check.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 质检相关配置，通过Nacos动态推送。
 * @author liyibo
 * @date 2026-07-15 15:46
 */
@Data
@Component
@ConfigurationProperties(prefix = "data-check")
public class CheckConfig {

    /** 每批扫描ID数量 */
    private int batchSize = 5000;

    /** 批次间休眠毫秒数，控制从库读取压力 */
    private long sleepBetweenBatch = 200;

    /** 全量质检开关 */
    private boolean fullCheckEnabled = true;

    /** 每日增量质检开关 */
    private boolean dailyCheckEnabled = true;

    /** 字段比对开关（关闭后只做记录级比对，不逐字段比较） */
    private boolean fieldCheckEnabled = true;

    /** 发现差异时是否发送告警（接入RocketMQ通知） */
    private boolean alertEnabled = true;
}
