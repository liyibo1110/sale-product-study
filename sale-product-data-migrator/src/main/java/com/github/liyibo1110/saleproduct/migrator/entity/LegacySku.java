package com.github.liyibo1110.saleproduct.migrator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PHP旧SKU宽表实体
 * @author liyibo
 * @date 2026-07-13 14:47
 */
@Data
@TableName("product_sku_legacy")
public class LegacySku {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private String skuCode;

    private String skuName;

    private Long price;

    private Long originalPrice;

    private Integer status;

    private String color;

    private String storage;

    private Integer weight;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
