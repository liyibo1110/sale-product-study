package com.github.liyibo1110.saleproduct.migrator.job;

import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import com.github.liyibo1110.saleproduct.migrator.config.MigrationConfig;
import com.github.liyibo1110.saleproduct.migrator.service.MigrationService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 增量迁移定时任务。
 * 每分钟执行一次，按update_time扫描变更记录，同步到新表。
 * @author liyibo
 * @date 2026-07-14 11:52
 */
@Component
public class IncrementalMigrationJob {

    private static final Logger log = LoggerFactory.getLogger(IncrementalMigrationJob.class);

    private final MigrationService migrationService;
    private final MigrationConfig config;

    public IncrementalMigrationJob(MigrationService migrationService, MigrationConfig config) {
        this.migrationService = migrationService;
        this.config = config;
    }

    @XxlJob("incrementalMigrationJob")
    public void execute() {
        if (!config.isIncrementalEnabled())
            return;

        StructuredLog.info(log)
                .message("增量迁移开始")
                .put("lookbackSeconds", config.getLookbackSeconds())
                .put("batchSize", config.getIncrementalBatchSize())
                .log();

        migrationService.executeIncrementalBatch();

        StructuredLog.info(log)
                .message("增量迁移本轮结束")
                .log();
    }
}
