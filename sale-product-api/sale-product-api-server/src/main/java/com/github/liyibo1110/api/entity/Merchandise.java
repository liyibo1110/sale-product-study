package com.github.liyibo1110.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品实体（读服务本地实体）
 * @author liyibo
 * @date 2026-07-22 14:53
 */
@Data
@TableName("merchandise")
public class Merchandise {
    private Long id;
    private String merchandiseName;
    private String merchandiseCode;
    private Integer status;
    private Long categoryId;
    private Long brandId;
    private String mainImage;
}
