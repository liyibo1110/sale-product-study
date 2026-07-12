package com.github.liyibo1110.saleproduct.admin.enums;

/**
 * @author liyibo
 * @date 2026-07-11 13:44
 */
public enum AttrType {

    STRING,
    INTEGER,
    DECIMAL,
    BOOLEAN,
    ENUM,
    JSON;

    public static AttrType of(String name) {
        for (AttrType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的属性类型: " + name);
    }
}
