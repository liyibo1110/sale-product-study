package com.github.liyibo1110.saleproduct.check.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据质检差异记录表，用来保存新旧表的比较差异。
 * @author liyibo
 * @date 2026-07-15 15:20
 */
@Data
@TableName("check_diff_log")
public class CheckDiffLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String checkType;

    private String sourceTable;

    private Long sourceId;

    private String fieldName;

    private String oldValue;

    private String newValue;

    private String batchNo;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
