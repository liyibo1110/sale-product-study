package com.github.liyibo1110.api.loader.data;

import lombok.Data;

import java.io.Serializable;

/**
 * BASE维度缓存数据
 * @author liyibo
 * @date 2026-07-23 11:44
 */
@Data
public class BaseMerchandiseData implements Serializable {
    private Long id;
    private String merchandiseCode;
    private String merchandiseName;
    private Integer status;
    private String mainImage;
    private Long categoryId;
    private Long brandId;
}
