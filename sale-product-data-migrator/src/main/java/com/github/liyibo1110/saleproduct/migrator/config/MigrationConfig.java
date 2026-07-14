package com.github.liyibo1110.saleproduct.migrator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 迁移配置，通过Nacos动态推送。
 * @author liyibo
 * @date 2026-07-13 14:59
 */
@Data
@Component
@ConfigurationProperties(prefix = "migration")
public class MigrationConfig {

    /** 全量迁移：每批大小 */
    private int fullBatchSize = 2000;

    /** 增量迁移：每批大小 */
    private int incrementalBatchSize = 5000;

    /** 批次间休眠毫秒数 */
    private long sleepBetweenBatch = 100;

    /** 增量迁移回溯窗口（秒） */
    private int lookbackSeconds = 30;

    /** 全量迁移开关 */
    private boolean fullMigrationEnabled = true;

    /** 增量迁移开关 */
    private boolean incrementalEnabled = true;

    /** 失败重试最大次数 */
    private int maxRetryCount = 5;
}
