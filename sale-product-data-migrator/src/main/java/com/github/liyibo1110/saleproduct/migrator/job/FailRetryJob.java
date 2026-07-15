package com.github.liyibo1110.saleproduct.migrator.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import com.github.liyibo1110.saleproduct.migrator.config.MigrationConfig;
import com.github.liyibo1110.saleproduct.migrator.entity.MigrationFailLog;
import com.github.liyibo1110.saleproduct.migrator.mapper.MigrationFailLogMapper;
import com.github.liyibo1110.saleproduct.migrator.service.MigrationService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 失败记录重试任务。
 * 定时扫描PENDING状态的失败记录，逐条重试。
 * @author liyibo
 * @date 2026-07-14 12:06
 */
@Component
public class FailRetryJob {

    private static final Logger log = LoggerFactory.getLogger(FailRetryJob.class);

    private final MigrationFailLogMapper failLogMapper;
    private final MigrationService migrationService;
    private final MigrationConfig config;

    public FailRetryJob(MigrationFailLogMapper failLogMapper,
                        MigrationService migrationService,
                        MigrationConfig config) {
        this.failLogMapper = failLogMapper;
        this.migrationService = migrationService;
        this.config = config;
    }

    @XxlJob("failRetryJob")
    public void execute() {
        List<MigrationFailLog> pendingList = failLogMapper.selectList(
                new LambdaQueryWrapper<MigrationFailLog>()
                        .eq(MigrationFailLog::getStatus, "PENDING")
                        .lt(MigrationFailLog::getRetryCount, config.getMaxRetryCount())
                        .last("LIMIT 500"));

        if (pendingList.isEmpty())
            return;

        StructuredLog.info(log)
                .message("失败重试开始")
                .put("pendingCount", pendingList.size())
                .log();

        int fixedCount = 0;
        for (MigrationFailLog failLog : pendingList) {
            boolean ok = migrationService.retryOne(failLog);
            if (ok)
                fixedCount++;
        }

        StructuredLog.info(log)
                .message("失败重试结束")
                .put("fixedCount", fixedCount)
                .put("totalRetried", pendingList.size())
                .log();
    }
}
