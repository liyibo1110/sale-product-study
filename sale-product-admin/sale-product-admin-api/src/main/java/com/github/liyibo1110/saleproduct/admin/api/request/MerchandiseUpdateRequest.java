package com.github.liyibo1110.saleproduct.admin.api.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品更新请求
 * @author liyibo
 * @date 2026-07-10 14:40
 */
@Data
public class MerchandiseUpdateRequest implements Serializable {

    private Long merchandiseId;

    private String merchandiseName;

    private Integer status;

    private Long categoryId;

    private Long brandId;

    private String mainImage;
}
