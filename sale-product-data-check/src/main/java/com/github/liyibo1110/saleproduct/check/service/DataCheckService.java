package com.github.liyibo1110.saleproduct.check.service;

import com.github.liyibo1110.saleproduct.check.config.CheckConfig;
import com.github.liyibo1110.saleproduct.check.mapper.CheckDiffLogMapper;
import com.github.liyibo1110.saleproduct.check.mapper.CheckProgressMapper;
import com.github.liyibo1110.saleproduct.check.mapper.DataCheckMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

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
}
