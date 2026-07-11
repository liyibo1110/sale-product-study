package com.github.liyibo1110.saleproduct.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 属性定义实体
 * @author liyibo
 * @date 2026-07-10 15:03
 */
@Data
@TableName("attribute_definition")
public class AttributeDefinition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String attrKey;

    private String attrName;

    /** 属性类型：STRING / INTEGER / DECIMAL / BOOLEAN / ENUM / JSON */
    private String attrType;

    /** 适用层级：MERCHANDISE / SIZE / ALL */
    private String applyLevel;

    private Integer required;

    /** 枚举值列表，JSON数组格式，如 ["VALUE1","VALUE2"] */
    private String enumValues;

    private String defaultValue;

    /** 最大长度，STRING类型使用 */
    private Integer maxLength;

    /** 最小值，数值类型使用 */
    private BigDecimal minValue;

    /** 最大值，数值类型使用 */
    private BigDecimal maxValue;

    private Integer status;

    private Integer sortOrder;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
