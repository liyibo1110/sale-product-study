package com.github.liyibo1110.saleproduct.admin.api.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 尺码保存请求。
 * @author liyibo
 * @date 2026-07-10 14:33
 */
@Data
public class SizeSaveRequest implements Serializable {

    private Long merchandiseId;

    private String sizeCode;

    private String sizeName;

    /** 售价（分） */
    private Long price;

    /** 原价（分） */
    private Long originalPrice;

    private Integer status;

    /** 规格数据JSON */
    private String specData;

    private Integer sortOrder;
}
