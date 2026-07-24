package com.github.liyibo1110.api.loader.data;

import lombok.Data;

import java.io.Serializable;

/**
 * CATEGORY维度缓存数据
 * @author liyibo
 * @date 2026-07-23 11:48
 */
@Data
public class CategoryData implements Serializable {
    private Long categoryId;
    private String categoryName;
    private String categoryPath;
}
