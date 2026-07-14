package com.github.liyibo1110.saleproduct.migrator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 迁移进度
 * @author liyibo
 * @date 2026-07-13 14:48
 */
@Data
@TableName("migration_progress")
public class MigrationProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskType;

    private Long lastId;

    private LocalDateTime lastScanTime;

    private String status;

    private Long migratedCount;

    private LocalDateTime updateTime;
}
