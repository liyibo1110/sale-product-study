package com.github.liyibo1110.saleproduct.check.job;

import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import com.github.liyibo1110.saleproduct.check.config.CheckConfig;
import com.github.liyibo1110.saleproduct.check.service.DataCheckService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 全量质检定时任务。
 * 建议在凌晨低峰期执行，完整扫描旧表和新表做四项比对。
 * @author liyibo
 * @date 2026-07-20 11:32
 */
@Component
public class DataCheckJob {

    private static final Logger log = LoggerFactory.getLogger(DataCheckJob.class);

    private final DataCheckService dataCheckService;
    private final CheckConfig config;

    public DataCheckJob(DataCheckService dataCheckService, CheckConfig config) {
        this.dataCheckService = dataCheckService;
        this.config = config;
    }

    /**
     * 全量质检：扫描全部数据，比对总量、缺失、多余、字段值
     * 调度策略：每天凌晨3点执行一次
     */
    @XxlJob("fullDataCheckJob")
    public void executeFullCheck() {
        if (!config.isFullCheckEnabled()) {
            StructuredLog.info(log)
                    .message("全量质检已关闭，跳过执行")
                    .log();
            return;
        }

        StructuredLog.info(log)
                .message("全量质检任务开始")
                .put("batchSize", config.getBatchSize())
                .put("fieldCheckEnabled", config.isFieldCheckEnabled())
                .log();

        long startTime = System.currentTimeMillis();

        dataCheckService.executeFullCheck();

        long cost = System.currentTimeMillis() - startTime;
        long pendingDiffs = dataCheckService.getPendingDiffCount();

        StructuredLog.info(log)
                .message("全量质检任务结束")
                .put("costMs", cost)
                .put("pendingDiffs", pendingDiffs)
                .log();
    }

    /**
     * 增量质检：只扫描最近24小时内有更新的记录
     * 调度策略：每小时执行一次（CRON: 0 0 * * * ?）
     * 适合在迁移期间持续追踪数据一致性
     */
    @XxlJob("dailyDataCheckJob")
    public void executeDailyCheck() {
        if (!config.isDailyCheckEnabled()) {
            StructuredLog.info(log)
                    .message("增量质检已关闭，跳过执行")
                    .log();
            return;
        }

        StructuredLog.info(log)
                .message("增量质检任务开始")
                .put("batchSize", config.getBatchSize())
                .log();

        long startTime = System.currentTimeMillis();

        dataCheckService.executeDailyCheck();

        long cost = System.currentTimeMillis() - startTime;
        long pendingDiffs = dataCheckService.getPendingDiffCount();

        StructuredLog.info(log)
                .message("增量质检任务结束")
                .put("costMs", cost)
                .put("pendingDiffs", pendingDiffs)
                .log();
    }
}
