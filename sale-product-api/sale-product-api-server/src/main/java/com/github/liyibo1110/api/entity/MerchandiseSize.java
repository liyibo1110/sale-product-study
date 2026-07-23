package com.github.liyibo1110.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品尺码实体（读服务本地实体）
 * @author liyibo
 * @date 2026-07-22 14:53
 */
@Data
@TableName("merchandise_size")
public class MerchandiseSize {
    private Long id;
    private Long merchandiseId;
    private String sizeCode;
    private String sizeName;
    private Long price;
    private Long originalPrice;
    private Integer status;
    private String specData;
    private Integer sortOrder;
}
