package com.github.liyibo1110.saleproduct.admin.api.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品属性值保存请求
 * @author liyibo
 * @date 2026-07-10 14:34
 */
@Data
public class AttributeValueSaveRequest implements Serializable {

    private Long merchandiseId;

    private String attrKey;

    private String attrValue;
}
