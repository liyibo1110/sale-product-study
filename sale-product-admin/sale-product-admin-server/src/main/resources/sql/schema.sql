-- 商品主表
CREATE TABLE IF NOT EXISTS merchandise (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    merchandise_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    merchandise_code VARCHAR(64) NOT NULL COMMENT '商品编码',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0下架 1上架',
    category_id BIGINT COMMENT '分类ID',
    brand_id BIGINT COMMENT '品牌ID',
    main_image VARCHAR(512) COMMENT '主图URL',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '更新人',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_merchandise_code (merchandise_code),
    KEY idx_category_id (category_id),
    KEY idx_brand_id (brand_id),
    KEY idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主表';

-- 商品尺码表
CREATE TABLE IF NOT EXISTS merchandise_size (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    merchandise_id BIGINT NOT NULL COMMENT '所属商品ID',
    size_code VARCHAR(64) NOT NULL COMMENT '尺码编码',
    size_name VARCHAR(200) NOT NULL COMMENT '尺码名称',
    price BIGINT NOT NULL COMMENT '售价（分）',
    original_price BIGINT COMMENT '原价（分）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
    spec_data VARCHAR(1000) COMMENT '规格数据JSON',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(64) COMMENT '创建人',
    update_by VARCHAR(64) COMMENT '更新人',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_size_code (size_code),
    KEY idx_merchandise_id (merchandise_id),
    KEY idx_update_time (update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品尺码表';

-- 属性定义表
CREATE TABLE IF NOT EXISTS attribute_definition (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    attr_key VARCHAR(64) NOT NULL COMMENT '属性键名',
    attr_name VARCHAR(100) NOT NULL COMMENT '属性显示名',
    attr_type VARCHAR(20) NOT NULL COMMENT '属性类型 STRING/INTEGER/DECIMAL/BOOLEAN/ENUM/JSON',
    apply_level VARCHAR(20) NOT NULL DEFAULT 'MERCHANDISE' COMMENT '适用层级 MERCHANDISE/SIZE/ALL',
    required TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填 0否 1是',
    enum_values VARCHAR(2000) COMMENT '枚举值列表JSON数组',
    default_value VARCHAR(500) COMMENT '默认值',
    max_length INT COMMENT '最大长度（STRING类型）',
    min_value DECIMAL(20,4) COMMENT '最小值（数值类型）',
    max_value DECIMAL(20,4) COMMENT '最大值（数值类型）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    description VARCHAR(500) COMMENT '属性描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_attr_key (attr_key),
    KEY idx_apply_level (apply_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='属性定义表';

-- 商品属性值表
CREATE TABLE IF NOT EXISTS merchandise_attribute_value (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    merchandise_id BIGINT NOT NULL COMMENT '所属商品ID',
    attribute_id BIGINT NOT NULL COMMENT '属性定义ID',
    attr_value VARCHAR(2000) COMMENT '属性值',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_merchandise_attr (merchandise_id, attribute_id),
    KEY idx_merchandise_id (merchandise_id),
    KEY idx_attribute_id (attribute_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品属性值表';

-- 尺码属性值表
CREATE TABLE IF NOT EXISTS merchandise_size_attribute_value (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    size_id BIGINT NOT NULL COMMENT '所属尺码ID',
    attribute_id BIGINT NOT NULL COMMENT '属性定义ID',
    attr_value VARCHAR(2000) COMMENT '属性值',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_size_attr (size_id, attribute_id),
    KEY idx_size_id (size_id),
    KEY idx_attribute_id (attribute_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='尺码属性值表';
