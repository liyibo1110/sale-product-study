package com.github.liyibo1110.api.loader.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * IMAGE维度缓存数据
 * @author liyibo
 * @date 2026-07-23 11:49
 */
@Data
public class ImageData implements Serializable {
    private List<String> carouselImages;
    private List<String> detailImages;
}
