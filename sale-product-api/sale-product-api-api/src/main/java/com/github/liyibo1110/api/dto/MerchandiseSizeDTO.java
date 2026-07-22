package com.github.liyibo1110.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 尺码（SKU）数据。
 * @author liyibo
 * @date 2026-07-21 10:18
 */
@Data
public class MerchandiseSizeDTO implements Serializable {

    private Long id;
    private String sizeCode;
    private String sizeName;
    private Long price;
    private Long originalPrice;
    private Integer status;
    private String specData;
    private Integer sortOrder;
    private Map<String, String> attributes;
}
