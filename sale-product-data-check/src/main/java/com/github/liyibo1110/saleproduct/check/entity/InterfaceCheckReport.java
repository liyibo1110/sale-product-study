package com.github.liyibo1110.saleproduct.check.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据质检查出来的疑似问题。
 * @author liyibo
 * @date 2026-07-15 15:23
 */
@Data
@TableName("interface_check_report")
public class InterfaceCheckReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String uri;

    private String merchandiseCode;

    private String phpField;

    private String javaField;

    private String phpValue;

    private String javaValue;

    private LocalDateTime checkTime;

    private String status;

    private String fixNote;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
