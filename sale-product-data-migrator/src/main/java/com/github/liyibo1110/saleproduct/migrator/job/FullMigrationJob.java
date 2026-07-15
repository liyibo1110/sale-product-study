package com.github.liyibo1110.saleproduct.migrator.job;

import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import com.github.liyibo1110.saleproduct.migrator.config.MigrationConfig;
import com.github.liyibo1110.saleproduct.migrator.service.MigrationService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 全量迁移定时任务。
 * 按ID范围分批扫描旧表，每次调度执行一轮循环（多个批次），直到全量完成。
 * @author liyibo
 * @date 2026-07-14 11:21
 */
@Component
public class FullMigrationJob {

    private static final Logger log = LoggerFactory.getLogger(FullMigrationJob.class);

    private final MigrationService migrationService;
    private final MigrationConfig config;

    public FullMigrationJob(MigrationService migrationService, MigrationConfig config) {
        this.migrationService = migrationService;
        this.config = config;
    }

    @XxlJob("fullMigrationJob")
    public void execute() {
        if (!config.isFullMigrationEnabled()) {
            StructuredLog.info(log)
                    .message("全量迁移已关闭，跳过执行")
                    .log();
            return;
        }

        StructuredLog.info(log)
                .message("全量迁移开始")
                .put("batchSize", config.getFullBatchSize())
                .log();

        long totalMigrated = 0;
        boolean hasMore = true;

        while (hasMore) {
            hasMore = migrationService.executeFullBatch();
            totalMigrated += config.getFullBatchSize();

            // 批次间休眠，控制对从库的读取压力
            if (hasMore)
                sleep(config.getSleepBetweenBatch());
        }

        StructuredLog.info(log)
                .message("全量迁移本轮结束")
                .put("processedApprox", totalMigrated)
                .log();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
