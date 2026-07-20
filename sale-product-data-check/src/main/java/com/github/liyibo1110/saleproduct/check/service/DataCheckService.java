package com.github.liyibo1110.saleproduct.check.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import com.github.liyibo1110.saleproduct.check.config.CheckConfig;
import com.github.liyibo1110.saleproduct.check.entity.CheckDiffLog;
import com.github.liyibo1110.saleproduct.check.entity.CheckProgress;
import com.github.liyibo1110.saleproduct.check.mapper.CheckDiffLogMapper;
import com.github.liyibo1110.saleproduct.check.mapper.CheckProgressMapper;
import com.github.liyibo1110.saleproduct.check.mapper.DataCheckMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据基础质检核心服务。
 * 对旧表(product_legacy)和新表(merchandise)做四项比对：
 * 1. 总量比对
 * 2. 缺失检测（旧表有、新表没有）
 * 3. 多余检测（新表有、旧表没有）
 * 4. 字段值比对（核心业务字段逐条比较）
 * @author liyibo
 * @date 2026-07-19 16:05
 */
@Service
public class DataCheckService {

    private static final Logger log = LoggerFactory.getLogger(DataCheckService.class);

    private static final String TASK_FULL_CHECK = "FULL_CHECK";
    private static final String TASK_EXTRA_CHECK = "EXTRA_CHECK";
    private static final String TASK_DAILY_CHECK = "DAILY_CHECK";
    private static final String SOURCE_TABLE = "product_legacy";
    private static final String TARGET_TABLE = "merchandise";
    private static final DateTimeFormatter BATCH_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // 核心比对字段映射：旧表字段名 -> 新表字段名
    private static final String[][] FIELD_MAPPINGS = {
            {"product_name", "merchandise_name"},
            {"status", "status"},
            {"category_id", "category_id"},
            {"brand_id", "brand_id"},
            {"main_image", "main_image"}
    };

    private final DataCheckMapper dataCheckMapper;
    private final CheckDiffLogMapper diffLogMapper;
    private final CheckProgressMapper progressMapper;
    private final CheckConfig config;

    public DataCheckService(DataCheckMapper dataCheckMapper, CheckDiffLogMapper diffLogMapper,
                            CheckProgressMapper progressMapper, CheckConfig config) {
        this.dataCheckMapper = dataCheckMapper;
        this.diffLogMapper = diffLogMapper;
        this.progressMapper = progressMapper;
        this.config = config;
    }

    /**
     * 执行全量质检（一次调度执行所有批次，直到全部扫描完毕）
     */
    public void executeFullCheck() {
        String batchNo = BATCH_FORMAT.format(LocalDateTime.now()) + "_FULL";

        // 第一步：总量比对
        checkCount(batchNo);

        // 第二步 + 第四步：正向扫描旧表，附带检测缺失和字段差异
        scanLegacyTable(batchNo);

        // 第三步：反向扫描新表，检测多余记录
        checkExtraInNew(batchNo);

        StructuredLog.info(log)
                .message("全量质检完成")
                .put("batchNo", batchNo)
                .put("pendingDiffs", getPendingDiffCount())
                .log();
    }

    /**
     * 执行增量质检（只扫描最近一天有变更的记录）
     */
    public void executeDailyCheck() {
        String batchNo = BATCH_FORMAT.format(LocalDateTime.now()) + "_DAILY";
        String startTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        CheckProgress progress = getOrCreateProgress(TASK_DAILY_CHECK);
        // 增量质检每次从头扫描（按时间范围过滤）
        progress.setLastId(0L);
        progress.setCheckedCount(0L);
        progress.setDiffCount(0L);
        progress.setStatus("RUNNING");
        progressMapper.updateById(progress);

        AtomicLong totalDiff = new AtomicLong(0);
        long lastId = 0;
        int batchSize = config.getBatchSize();
        boolean hasMore = true;

        while (hasMore) {
            // 和全量检测的区别，就在于这里的startTime是1天前
            List<Map<String, Object>> batch = dataCheckMapper.selectLegacyIncrementalBatch(startTime, lastId, batchSize);
            if (batch.isEmpty())
                break;

            int batchDiff = processBatch(batch, batchNo);
            totalDiff.addAndGet(batchDiff);

            lastId = ((Number) batch.get(batch.size() - 1).get("id")).longValue();

            progress.setLastId(lastId);
            progress.setCheckedCount(progress.getCheckedCount() + batch.size());
            progress.setDiffCount(totalDiff.get());
            progressMapper.updateById(progress);

            hasMore = batch.size() == batchSize;
            if (hasMore)
                sleep(config.getSleepBetweenBatch());
        }

        progress.setStatus("COMPLETED");
        progressMapper.updateById(progress);

        StructuredLog.info(log)
                .message("增量质检完成")
                .put("batchNo", batchNo)
                .put("checkedCount", progress.getCheckedCount())
                .put("diffCount", totalDiff.get())
                .log();
    }

    /**
     * 总量比对：新表COUNT vs 旧表COUNT
     */
    private void checkCount(String batchNo) {
        long legacyCount = dataCheckMapper.countLegacyProducts();
        long newCount = dataCheckMapper.countNewMerchandise();

        StructuredLog.info(log)
                .message("总量比对")
                .put("legacyCount", legacyCount)
                .put("newCount", newCount)
                .log();

        if (legacyCount != newCount) {
            CheckDiffLog diff = new CheckDiffLog();
            diff.setCheckType("COUNT_MISMATCH");
            diff.setSourceTable(SOURCE_TABLE);
            diff.setFieldName("total_count");
            diff.setOldValue(String.valueOf(legacyCount));
            diff.setNewValue(String.valueOf(newCount));
            diff.setBatchNo(batchNo);
            diff.setStatus("PENDING");
            diffLogMapper.insert(diff);
        }
    }

    /**
     * 正向扫描旧表：缺失检测 + 字段比对
     */
    private void scanLegacyTable(String batchNo) {
        CheckProgress progress = getOrCreateProgress(TASK_FULL_CHECK);
        // 以前已完成了，则重置这一条的状态
        if ("COMPLETED".equals(progress.getStatus())) {
            progress.setLastId(0L);
            progress.setCheckedCount(0L);
            progress.setDiffCount(0L);
            progress.setStatus("RUNNING");
            progressMapper.updateById(progress);
        }

        AtomicLong totalDiff = new AtomicLong(progress.getDiffCount());
        long lastId = progress.getLastId();
        int batchSize = config.getBatchSize();
        boolean hasMore = true;

        while (hasMore) {
            // 1、取下一批要检测的旧数据
            List<Map<String, Object>> batch = dataCheckMapper.selectLegacyIdAndCodeBatch(lastId, batchSize);
            if (batch.isEmpty())
                break;

            // 2、开始字段对比检测
            int batchDiff = processBatch(batch, batchNo);
            totalDiff.addAndGet(batchDiff);

            // 3、重置lastId
            lastId = ((Number) batch.get(batch.size() - 1).get("id")).longValue();
            progress.setLastId(lastId);
            progress.setCheckedCount(progress.getCheckedCount() + batch.size());
            progress.setDiffCount(totalDiff.get());
            progressMapper.updateById(progress);

            // 4、根据上一批的返回数量，推测是否全部完成
            hasMore = batch.size() == batchSize;
            if (hasMore)
                sleep(config.getSleepBetweenBatch());
        }

        progress.setStatus("COMPLETED");
        progressMapper.updateById(progress);
    }

    /**
     * 反向扫描新表：多余检测（新表有、旧表没有）
     */
    private void checkExtraInNew(String batchNo) {
        CheckProgress progress = getOrCreateProgress(TASK_EXTRA_CHECK);
        // 以前已完成了，则重置这一条的状态
        if ("COMPLETED".equals(progress.getStatus())) {
            progress.setLastId(0L);
            progress.setCheckedCount(0L);
            progress.setDiffCount(0L);
            progress.setStatus("RUNNING");
            progressMapper.updateById(progress);
        }

        AtomicLong totalDiff = new AtomicLong(progress.getDiffCount());
        long lastId = progress.getLastId();
        int batchSize = config.getBatchSize();
        boolean hasMore = true;

        while (hasMore) {
            // 获取新库的商品
            List<Map<String, Object>> batch = dataCheckMapper.selectNewIdAndCodeBatch(lastId, batchSize);
            if (batch.isEmpty())
                break;

            // 收集本批merchandise_code
            List<String> codes = new ArrayList<>(batch.size());
            for (Map<String, Object> row : batch)
                codes.add((String) row.get("merchandise_code"));

            // 批量查询旧表中哪些code存在
            List<String> existingCodes = dataCheckMapper.selectExistingLegacyCodes(codes);
            Set<String> existingSet = new HashSet<>(existingCodes);

            for (Map<String, Object> row : batch) {
                String code = (String) row.get("merchandise_code");
                long newId = ((Number) row.get("id")).longValue();

                if (!existingSet.contains(code)) {
                    // 新表有、旧表没有：多余记录
                    CheckDiffLog diff = new CheckDiffLog();
                    diff.setCheckType("EXTRA_IN_NEW");
                    diff.setSourceTable(TARGET_TABLE);
                    diff.setSourceId(newId);
                    diff.setFieldName("merchandise_code");
                    diff.setOldValue(null);
                    diff.setNewValue(code);
                    diff.setBatchNo(batchNo);
                    diff.setStatus("PENDING");
                    diffLogMapper.insert(diff);
                    totalDiff.incrementAndGet();
                }
            }

            lastId = ((Number) batch.get(batch.size() - 1).get("id")).longValue();

            progress.setLastId(lastId);
            progress.setCheckedCount(progress.getCheckedCount() + batch.size());
            progress.setDiffCount(totalDiff.get());
            progressMapper.updateById(progress);

            hasMore = batch.size() == batchSize;
            if (hasMore)
                sleep(config.getSleepBetweenBatch());
        }

        progress.setStatus("COMPLETED");
        progressMapper.updateById(progress);

        StructuredLog.info(log)
                .message("多余记录检测完成")
                .put("batchNo", batchNo)
                .put("extraCount", totalDiff.get())
                .log();
    }

    /**
     * 处理一批数据：缺失检测 + 字段比对
     * @return 本批发现的差异数
     */
    private int processBatch(List<Map<String, Object>> batch, String batchNo) {
        int diffCount = 0;

        // 收集本批所有product_code（旧表）
        List<String> codes = new ArrayList<>(batch.size());
        for (Map<String, Object> row : batch)
            codes.add((String) row.get("product_code"));

        // 批量查询新表中存在哪些code
        List<String> existingCodes = dataCheckMapper.selectExistingCodes(codes);
        Set<String> existingSet = new HashSet<>(existingCodes);

        for (Map<String, Object> row : batch) {
            String code = (String) row.get("product_code");
            long sourceId = ((Number) row.get("id")).longValue();

            if (!existingSet.contains(code)) {
                // 缺失：旧表有，但新表没有
                CheckDiffLog diff = new CheckDiffLog();
                diff.setCheckType("MISSING_IN_NEW");
                diff.setSourceTable(SOURCE_TABLE);
                diff.setSourceId(sourceId);
                diff.setFieldName("merchandise_code");
                diff.setOldValue(code);
                diff.setNewValue(null);
                diff.setBatchNo(batchNo);
                diff.setStatus("PENDING");
                diffLogMapper.insert(diff);
                diffCount++;
                continue;
            }

            // 字段比对
            if (config.isFieldCheckEnabled())
                diffCount += compareFields(sourceId, code, batchNo);
        }

        return diffCount;
    }

    /**
     * 逐字段比对一条记录
     */
    private int compareFields(long sourceId, String merchandiseCode, String batchNo) {
        Map<String, Object> legacy = dataCheckMapper.selectLegacyById(sourceId);
        Map<String, Object> newRecord = dataCheckMapper.selectNewByCode(merchandiseCode);

        if (legacy == null || newRecord == null)
            return 0;

        int diffCount = 0;
        for (String[] mapping : FIELD_MAPPINGS) {
            String oldField = mapping[0];
            String newField = mapping[1];

            String oldValue = toString(legacy.get(oldField));
            String newValue = toString(newRecord.get(newField));

            if (!Objects.equals(oldValue, newValue)) {
                CheckDiffLog diff = new CheckDiffLog();
                diff.setCheckType("FIELD_MISMATCH");
                diff.setSourceTable(SOURCE_TABLE);
                diff.setSourceId(sourceId);
                diff.setFieldName(oldField + " -> " + newField);
                diff.setOldValue(oldValue);
                diff.setNewValue(newValue);
                diff.setBatchNo(batchNo);
                diff.setStatus("PENDING");
                diffLogMapper.insert(diff);
                diffCount++;
            }
        }

        return diffCount;
    }

    /**
     * 获取质检摘要：当前有多少PENDING差异
     */
    public long getPendingDiffCount() {
        return diffLogMapper.selectCount(new LambdaQueryWrapper<CheckDiffLog>().eq(CheckDiffLog::getStatus, "PENDING"));
    }

    private CheckProgress getOrCreateProgress(String taskType) {
        CheckProgress progress = progressMapper.selectOne(
                new LambdaQueryWrapper<CheckProgress>()
                        .eq(CheckProgress::getTaskType, taskType)
                        .eq(CheckProgress::getSourceTable, SOURCE_TABLE));
        if (progress == null) {
            progress = new CheckProgress();
            progress.setTaskType(taskType);
            progress.setSourceTable(SOURCE_TABLE);
            progress.setLastId(0L);
            progress.setStatus("RUNNING");
            progress.setCheckedCount(0L);
            progress.setDiffCount(0L);
            progressMapper.insert(progress);
        }
        return progress;
    }

    private String toString(Object value) {
        if (value == null)
            return null;

        return value.toString();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
