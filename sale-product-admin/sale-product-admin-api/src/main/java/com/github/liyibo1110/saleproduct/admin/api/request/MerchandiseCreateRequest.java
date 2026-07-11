package com.github.liyibo1110.saleproduct.admin.api.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 商品创建请求。
 * @author liyibo
 * @date 2026-07-10 14:08
 */
@Data
public class MerchandiseCreateRequest implements Serializable {

    private String merchandiseName;

    private String merchandiseCode;

    private Long categoryId;

    private Long brandId;

    private String mainImage;

    /** 尺码列表 */
    private List<SizeItem> sizeList;

    /** 商品维度的动态属性，key=attr_key，value=属性值 */
    private Map<String, String> attributes;

    /**
     * 商品尺码
     */
    @Data
    public static class SizeItem implements Serializable {
        private String sizeCode;

        private String sizeName;

        /** 售价（分） */
        private Long price;

        /** 原价（分） */
        private Long originalPrice;

        /** 规格数据JSON */
        private String specData;

        private Integer sortOrder;

        /** 尺码维度的动态属性，key=attr_key，value=属性值 */
        private Map<String, String> sizeAttributes;
    }
}
