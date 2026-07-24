package com.github.liyibo1110.api.loader.data;

import lombok.Data;

import java.io.Serializable;

/**
 * @author liyibo
 * @date 2026-07-23 11:49
 */
@Data
public class PriceData implements Serializable {
    private Long price;
    private Long originalPrice;
}
