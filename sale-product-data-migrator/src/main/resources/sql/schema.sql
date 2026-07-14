-- ============================================================
-- 模拟PHP旧表（宽表结构）
-- 生产环境中这张表在PHP专属从库里，这里用同一个schema模拟
-- ============================================================
CREATE TABLE IF NOT EXISTS product_legacy (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_code VARCHAR(64) NOT NULL COMMENT '商品编码',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0下架 1上架',
    category_id BIGINT COMMENT '分类ID',
    brand_id BIGINT COMMENT '品牌ID',
    main_image VARCHAR(512) COMMENT '主图URL',
    price BIGINT NOT NULL DEFAULT 0 COMMENT '售价（分）',
    original_price BIGINT COMMENT '原价（分）',
    is_hot TINYINT NOT NULL DEFAULT 0 COMMENT '是否爆款',
    is_new TINYINT NOT NULL DEFAULT 0 COMMENT '是否新品',
    target_audience VARCHAR(100) COMMENT '适用人群',
    network_type VARCHAR(200) COMMENT '网络制式（枚举值）',
    origin VARCHAR(100) COMMENT '产地',
    warranty_months INT COMMENT '保修月数',
    extra_info TEXT COMMENT '扩展信息JSON',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_code (product_code),
    KEY idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PHP旧商品宽表（模拟）';

-- PHP旧SKU表
CREATE TABLE IF NOT EXISTS product_sku_legacy (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    product_id BIGINT NOT NULL COMMENT '所属商品ID',
    sku_code VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    sku_name VARCHAR(200) NOT NULL COMMENT 'SKU名称',
    price BIGINT NOT NULL DEFAULT 0 COMMENT '售价（分）',
    original_price BIGINT COMMENT '原价（分）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    color VARCHAR(50) COMMENT '颜色',
    storage VARCHAR(50) COMMENT '存储容量',
    weight INT COMMENT '克重（克）',
    sort_order INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sku_code (sku_code),
    KEY idx_product_id (product_id),
    KEY idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PHP旧SKU宽表（模拟）';

-- ============================================================
-- 迁移服务自身的表
-- ============================================================

-- 迁移进度表
CREATE TABLE IF NOT EXISTS migration_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_type VARCHAR(32) NOT NULL COMMENT '任务类型 FULL_PRODUCT/FULL_SKU/INCREMENTAL_PRODUCT/INCREMENTAL_SKU',
    last_id BIGINT NOT NULL DEFAULT 0 COMMENT '全量迁移：上次扫描到的最大ID',
    last_scan_time DATETIME COMMENT '增量迁移：上次扫描的截止时间',
    status VARCHAR(16) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/COMPLETED/PAUSED',
    migrated_count BIGINT NOT NULL DEFAULT 0 COMMENT '已迁移条数',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_type (task_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='迁移进度表';

-- 迁移失败日志表
CREATE TABLE IF NOT EXISTS migration_fail_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    source_table VARCHAR(64) NOT NULL COMMENT '来源表名',
    source_id BIGINT NOT NULL COMMENT '来源记录主键',
    error_type VARCHAR(32) NOT NULL COMMENT '错误类型 RPC_TIMEOUT/VALIDATION_FAILED/TRANSFORM_ERROR/UNKNOWN',
    error_message VARCHAR(2000) COMMENT '错误信息',
    source_data TEXT COMMENT '原始数据JSON快照',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '已重试次数',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/FIXED/IGNORED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_source (source_table, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='迁移失败日志表';
