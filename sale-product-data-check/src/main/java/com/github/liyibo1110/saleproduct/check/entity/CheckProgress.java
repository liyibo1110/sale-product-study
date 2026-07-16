package com.github.liyibo1110.saleproduct.check.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据质检进度任务记录。
 * @author liyibo
 * @date 2026-07-15 15:16
 */
@Data
@TableName("check_progress")
public class CheckProgress {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskType;

    private String sourceTable;

    private Long lastId;

    private String status;

    private Long checkedCount;

    private Long diffCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
