package com.github.liyibo1110.saleproduct.admin.api.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 尺码属性值保存请求
 * @author liyibo
 * @date 2026-07-10 14:39
 */
@Data
public class MerchandiseSizeAttributeValueSaveRequest implements Serializable {

    private Long sizeId;

    private String attrKey;

    private String attrValue;
}
