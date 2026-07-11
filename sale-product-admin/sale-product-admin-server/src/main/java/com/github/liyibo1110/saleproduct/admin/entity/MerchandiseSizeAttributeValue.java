package com.github.liyibo1110.saleproduct.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 尺码属性值实体
 * @author liyibo
 * @date 2026-07-10 15:06
 */
@Data
@TableName("merchandise_size_attribute_value")
public class MerchandiseSizeAttributeValue {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sizeId;

    private Long attributeId;

    private String attrValue;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
