package com.github.liyibo1110.api.loader.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * SKU维度缓存数据
 * @author liyibo
 * @date 2026-07-23 11:50
 */
@Data
public class SkuData implements Serializable {
    private List<SkuItem> items;

    @Data
    public static class SkuItem implements Serializable {
        private Long id;
        private String sizeCode;
        private String sizeName;
        private Long price;
        private Long originalPrice;
        private Integer status;
        private String specData;
        private Integer sortOrder;
    }
}
