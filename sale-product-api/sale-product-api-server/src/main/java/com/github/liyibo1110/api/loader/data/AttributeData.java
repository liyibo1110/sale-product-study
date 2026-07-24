package com.github.liyibo1110.api.loader.data;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * ATTRIBUTE维度缓存数据
 * @author liyibo
 * @date 2026-07-23 11:40
 */
@Data
public class AttributeData implements Serializable {
    /* key是attr_key，value是attr_value */
    private Map<String, String> merchandiseAttributes;

    /* key是sizeCode，value是该尺码的属性Map */
    private Map<String, Map<String, String>> sizeAttributes;
}
