package com.github.liyibo1110.saleproduct.migrator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 迁移失败日志
 * @author liyibo
 * @date 2026-07-13 14:47
 */
@Data
@TableName("migration_fail_log")
public class MigrationFailLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceTable;

    private Long sourceId;

    private String errorType;

    private String errorMessage;

    private String sourceData;

    private Integer retryCount;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
