package com.github.liyibo1110.saleproduct.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liyibo1110.saleproduct.base.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品实体
 * @author liyibo
 * @date 2026-07-10 14:56
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchandise")
public class Merchandise extends BaseEntity {

    private String merchandiseName;

    private String merchandiseCode;

    private Integer status;

    private Long categoryId;

    private Long brandId;

    private String mainImage;
}
