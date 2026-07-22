package com.github.liyibo1110.api.request;

import com.github.liyibo1110.api.enums.QueryOption;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 商品查询请求参数。
 * @author liyibo
 * @date 2026-07-21 10:20
 */
@Data
public class MerchandiseQueryRequest implements Serializable {

    /** 商品编码（单个查询） */
    private String merchandiseCode;

    /** 商品编码列表（批量查询） */
    private List<String> merchandiseCodes;

    /** 查询选项，不传则默认BASE */
    private Set<QueryOption> options;
}
