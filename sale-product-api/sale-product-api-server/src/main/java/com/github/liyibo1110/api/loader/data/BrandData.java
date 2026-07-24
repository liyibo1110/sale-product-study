package com.github.liyibo1110.api.loader.data;

import lombok.Data;

import java.io.Serializable;

/**
 * BRAND维度缓存数据
 * @author liyibo
 * @date 2026-07-23 11:45
 */
@Data
public class BrandData implements Serializable {
    private Long brandId;
    private String brandName;
    private String brandLogo;
}
