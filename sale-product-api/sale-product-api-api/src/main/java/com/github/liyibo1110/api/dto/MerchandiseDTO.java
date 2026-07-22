package com.github.liyibo1110.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 商品读接口统一响应体。
 * 各字段按调用方传入的QueryOption按需填充，未请求的维度为null。
 * @author liyibo
 * @date 2026-07-21 10:15
 */
@Data
public class MerchandiseDTO implements Serializable {

    // ===== BASE =====
    private Long id;
    private String merchandiseCode;
    private String merchandiseName;
    private Integer status;
    private String mainImage;

    // ===== PRICE（取自第一个有效SKU） =====
    private Long price;
    private Long originalPrice;

    // ===== SKU =====
    private List<MerchandiseSizeDTO> sizes;

    // ===== BRAND =====
    private Long brandId;
    private String brandName;
    private String brandLogo;

    // ===== CATEGORY =====
    private Long categoryId;
    private String categoryName;
    private String categoryPath;

    // ===== IMAGE =====
    private List<String> carouselImages;
    private List<String> detailImages;

    // ===== ATTRIBUTE =====
    private Map<String, String> attributes;

    // ===== DESCRIPTION =====
    private String description;
}
