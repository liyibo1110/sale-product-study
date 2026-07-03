package com.github.liyibo1110.saleproduct.base.result;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liyibo
 * @date 2026-07-02 11:06
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private long pageNum = 1;

    private long pageSize = 10;
}
