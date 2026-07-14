package com.github.liyibo1110.saleproduct.migrator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PHP旧商品宽表实体
 * @author liyibo
 * @date 2026-07-13 14:47
 */
@Data
@TableName("product_legacy")
public class LegacyProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String productName;

    private String productCode;

    private Integer status;

    private Long categoryId;

    private Long brandId;

    private String mainImage;

    private Long price;

    private Long originalPrice;

    private Integer isHot;

    private Integer isNew;

    private String targetAudience;

    private String networkType;

    private String origin;

    private Integer warrantyMonths;

    private String extraInfo;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
