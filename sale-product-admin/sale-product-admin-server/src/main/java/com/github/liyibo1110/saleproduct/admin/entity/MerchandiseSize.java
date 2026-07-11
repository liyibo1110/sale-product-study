package com.github.liyibo1110.saleproduct.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liyibo1110.saleproduct.base.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品尺码实体
 * @author liyibo
 * @date 2026-07-10 15:05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchandise_size")
public class MerchandiseSize extends BaseEntity {

    private Long merchandiseId;

    private String sizeCode;

    private String sizeName;

    /** 售价，单位：分 */
    private Long price;

    /** 原价，单位：分 */
    private Long originalPrice;

    private Integer status;

    /** 规格数据，JSON格式 */
    private String specData;

    private Integer sortOrder;
}
