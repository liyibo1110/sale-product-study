package com.github.liyibo1110.saleproduct.admin.enums;

/**
 * 商品状态枚举
 * @author liyibo
 * @date 2026-07-11 13:43
 */
public enum MerchandiseStatus {

    OFF_SHELF(0, "下架"),
    ON_SHELF(1, "上架");

    private final int code;
    private final String desc;

    MerchandiseStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
