-- 质检结果表
CREATE TABLE check_diff_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    check_type VARCHAR(32) NOT NULL COMMENT 'COUNT_MISMATCH/MISSING_IN_NEW/EXTRA_IN_NEW/FIELD_MISMATCH',
    source_table VARCHAR(64) NOT NULL COMMENT '来源表名',
    source_id BIGINT COMMENT '来源记录主键',
    field_name VARCHAR(64) COMMENT '不一致的字段名',
    old_value VARCHAR(2000) COMMENT '旧表的值',
    new_value VARCHAR(2000) COMMENT '新表的值',
    batch_no VARCHAR(64) NOT NULL COMMENT '批次号，格式：yyyyMMdd_HHmmss_seq',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/FIXED/IGNORED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_batch_no (batch_no),
    KEY idx_status (status),
    KEY idx_source (source_table, source_id),
    KEY idx_check_type (check_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基础质检差异日志';

-- 质检进度表
CREATE TABLE check_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_type VARCHAR(32) NOT NULL COMMENT 'FULL_CHECK/DAILY_CHECK',
    source_table VARCHAR(64) NOT NULL COMMENT '质检的来源表',
    last_id BIGINT NOT NULL DEFAULT 0 COMMENT '上次处理到的最大ID',
    status VARCHAR(16) NOT NULL DEFAULT 'RUNNING' COMMENT 'RUNNING/COMPLETED',
    checked_count BIGINT NOT NULL DEFAULT 0 COMMENT '已检查记录数',
    diff_count BIGINT NOT NULL DEFAULT 0 COMMENT '发现差异数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_table (task_type, source_table)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质检进度表';

-- 接口级质检报告表
CREATE TABLE interface_check_report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    uri VARCHAR(200) NOT NULL COMMENT '接口路径',
    merchandise_code VARCHAR(64) COMMENT '商品编码',
    php_field VARCHAR(200) COMMENT 'PHP侧字段路径',
    java_field VARCHAR(200) COMMENT 'Java侧字段路径',
    php_value VARCHAR(2000) COMMENT 'PHP侧的值',
    java_value VARCHAR(2000) COMMENT 'Java侧的值',
    check_time DATETIME NOT NULL COMMENT '校验时间',
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING/FIXED/IGNORED/KNOWN_DIFF',
    fix_note VARCHAR(500) COMMENT '修复说明',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_uri (uri),
    KEY idx_merchandise_code (merchandise_code),
    KEY idx_status (status),
    KEY idx_check_time (check_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口级质检报告';
