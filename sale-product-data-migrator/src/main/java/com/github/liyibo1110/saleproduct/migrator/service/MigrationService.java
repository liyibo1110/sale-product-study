package com.github.liyibo1110.saleproduct.migrator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.saleproduct.admin.api.MerchandiseWriteService;
import com.github.liyibo1110.saleproduct.admin.api.request.MerchandiseCreateRequest;
import com.github.liyibo1110.saleproduct.base.result.Result;
import com.github.liyibo1110.saleproduct.migrator.config.MigrationConfig;
import com.github.liyibo1110.saleproduct.migrator.converter.LegacyProductConverter;
import com.github.liyibo1110.saleproduct.migrator.entity.LegacyProduct;
import com.github.liyibo1110.saleproduct.migrator.entity.LegacySku;
import com.github.liyibo1110.saleproduct.migrator.entity.MigrationFailLog;
import com.github.liyibo1110.saleproduct.migrator.entity.MigrationProgress;
import com.github.liyibo1110.saleproduct.migrator.mapper.LegacyProductMapper;
import com.github.liyibo1110.saleproduct.migrator.mapper.LegacySkuMapper;
import com.github.liyibo1110.saleproduct.migrator.mapper.MigrationFailLogMapper;
import com.github.liyibo1110.saleproduct.migrator.mapper.MigrationProgressMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据迁移核心服务。
 * 负责从旧表读取数据，调用转换器转为新格式，通过RPC写入admin服务。
 * @author liyibo
 * @date 2026-07-14 10:30
 */
@Service
public class MigrationService {

    private static final Logger log = LoggerFactory.getLogger(MigrationService.class);

    private static final String TASK_FULL_PRODUCT = "FULL_PRODUCT";
    private static final String TASK_INCREMENTAL_PRODUCT = "INCREMENTAL_PRODUCT";

    @DubboReference
    private MerchandiseWriteService merchandiseWriteService;

    private final LegacyProductMapper legacyProductMapper;
    private final LegacySkuMapper legacySkuMapper;
    private final MigrationProgressMapper progressMapper;
    private final MigrationFailLogMapper failLogMapper;
    private final LegacyProductConverter converter;
    private final MigrationConfig config;
    private final ObjectMapper objectMapper;

    public MigrationService(LegacyProductMapper legacyProductMapper,
                            LegacySkuMapper legacySkuMapper,
                            MigrationProgressMapper progressMapper,
                            MigrationFailLogMapper failLogMapper,
                            LegacyProductConverter converter,
                            MigrationConfig config,
                            ObjectMapper objectMapper) {
        this.legacyProductMapper = legacyProductMapper;
        this.legacySkuMapper = legacySkuMapper;
        this.progressMapper = progressMapper;
        this.failLogMapper = failLogMapper;
        this.converter = converter;
        this.config = config;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行一轮全量迁移（处理一个批次）
     * @return true=还有下一批，false=全量迁移完成
     */
    public boolean executeFullBatch() {
        MigrationProgress progress = getOrCreateProgress(TASK_FULL_PRODUCT);
        // 如果最后一条记录是“已完成”，说明整个全量迁移已完成
        if ("COMPLETED".equals(progress.getStatus()))
            return false;

        long lastId = progress.getLastId();
        int batchSize = config.getFullBatchSize();

        // 获取一批原始数据
        List<LegacyProduct> batch = legacyProductMapper.selectBatchByIdRange(lastId, batchSize);
        // 结果为空，说明已经没有旧数据要迁移了，全量迁移完成，将最后一条记录标记成“已完成”
        if (batch.isEmpty()) {
            progress.setStatus("COMPLETED");
            progress.setLastScanTime(LocalDateTime.now());
            progressMapper.updateById(progress);
            return false;
        }

        // 开始迁移这批数据
        long successCount = 0;
        for (LegacyProduct legacy : batch) {
            boolean ok = migrateOneProduct(legacy);
            if (ok)
                successCount++;
        }

        // 更新进度
        long maxId = batch.get(batch.size() - 1).getId();
        progress.setLastId(maxId);
        progress.setMigratedCount(progress.getMigratedCount() + successCount);
        progressMapper.updateById(progress);

        return batch.size() == batchSize;
    }

    /**
     * 执行一轮增量迁移
     */
    public void executeIncrementalBatch() {
        MigrationProgress progress = getOrCreateProgress(TASK_INCREMENTAL_PRODUCT);

        // 回溯窗口：start_time = last_scan_time - lookbackSeconds
        LocalDateTime lastScanTime = progress.getLastScanTime();
        if (lastScanTime == null) {
            // 增量迁移首次执行，从全量迁移完成时间开始
            MigrationProgress fullProgress = getOrCreateProgress(TASK_FULL_PRODUCT);
            lastScanTime = fullProgress.getLastScanTime();
            if (lastScanTime == null)
                lastScanTime = LocalDateTime.now().minusMinutes(5);
        }

        LocalDateTime startTime = lastScanTime.minusSeconds(config.getLookbackSeconds());
        int batchSize = config.getIncrementalBatchSize();

        List<LegacyProduct> batch = legacyProductMapper.selectByUpdateTime(startTime, batchSize);
        if (batch.isEmpty())
            return;

        for (LegacyProduct legacy : batch)
            migrateOneProduct(legacy);

        // 更新last_scan_time为本批最大的update_time
        LocalDateTime maxUpdateTime = batch.stream()
                .map(LegacyProduct::getUpdateTime)
                .max(LocalDateTime::compareTo)
                .orElse(lastScanTime);

        progress.setLastScanTime(maxUpdateTime);
        progress.setMigratedCount(progress.getMigratedCount() + batch.size());
        progressMapper.updateById(progress);
    }

    /**
     * 迁移单个商品（含其下所有SKU）
     */
    private boolean migrateOneProduct(LegacyProduct legacy) {
        try {
            // 读取该商品下的所有SKU
            List<LegacySku> skuList = legacySkuMapper.selectByProductId(legacy.getId());

            // 转换
            MerchandiseCreateRequest request = converter.convert(legacy, skuList);

            // RPC调用admin服务写入
            Result<Long> result = merchandiseWriteService.createMerchandise(request);
            if (!Result.isSuccess(result)) {
                recordFailure(legacy, "VALIDATION_FAILED", result.getMessage());
                return false;
            }

            return true;

        } catch (Exception e) {
            String errorType = classifyError(e);
            recordFailure(legacy, errorType, e.getMessage());
            return false;
        }
    }

    /**
     * 重试单条失败记录
     */
    public boolean retryOne(MigrationFailLog failLog) {
        LegacyProduct legacy = legacyProductMapper.selectById(failLog.getSourceId());
        if (legacy == null) {
            failLog.setStatus("IGNORED");
            failLog.setRetryCount(failLog.getRetryCount() + 1);
            failLogMapper.updateById(failLog);
            return false;
        }

        boolean ok = migrateOneProduct(legacy);
        failLog.setRetryCount(failLog.getRetryCount() + 1);
        if (ok)
            failLog.setStatus("FIXED");

        failLogMapper.updateById(failLog);
        return ok;
    }

    private void recordFailure(LegacyProduct legacy, String errorType, String message) {
        MigrationFailLog failLog = new MigrationFailLog();
        failLog.setSourceTable("product_legacy");
        failLog.setSourceId(legacy.getId());
        failLog.setErrorType(errorType);
        failLog.setErrorMessage(truncate(message, 2000));
        failLog.setStatus("PENDING");
        failLog.setRetryCount(0);

        try {
            failLog.setSourceData(objectMapper.writeValueAsString(legacy));
        } catch (Exception e) {
            failLog.setSourceData("{}");
        }

        failLogMapper.insert(failLog);
    }

    /**
     * 将迁移过程中的Exception，转换成可读的错误类型描述。
     */
    private String classifyError(Exception e) {
        String className = e.getClass().getSimpleName();
        if (className.contains("Timeout") || className.contains("TimeoutException"))
            return "RPC_TIMEOUT";

        if (className.contains("Validation") || className.contains("IllegalArgument"))
            return "VALIDATION_FAILED";

        if (className.contains("JsonProcessing") || className.contains("NumberFormat"))
            return "TRANSFORM_ERROR";

        return "UNKNOWN";
    }

    /**
     * 获取上一次的progress记录，如果没有任何记录，则先创建新的记录。
     */
    private MigrationProgress getOrCreateProgress(String taskType) {
        MigrationProgress progress = progressMapper.selectOne(
                new LambdaQueryWrapper<MigrationProgress>()
                        .eq(MigrationProgress::getTaskType, taskType));
        if (progress == null) {
            progress = new MigrationProgress();
            progress.setTaskType(taskType);
            progress.setLastId(0L);
            progress.setStatus("RUNNING");
            progress.setMigratedCount(0L);
            progressMapper.insert(progress);
        }
        return progress;
    }

    private String truncate(String value, int maxLength) {
        if (value == null)
            return null;

        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
