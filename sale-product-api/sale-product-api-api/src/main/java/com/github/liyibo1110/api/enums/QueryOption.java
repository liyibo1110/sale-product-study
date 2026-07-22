package com.github.liyibo1110.api.enums;

/**
 * 商品查询选项。
 * 调用方通过组合选项声明需要哪些维度的数据，读服务按需组装返回。
 * @author liyibo
 * @date 2026-07-21 10:19
 */
public enum QueryOption {

    /** 基础数据：商品名称、状态、主图、编码 */
    BASE,

    /** 价格数据：售价、原价 */
    PRICE,

    /** SKU数据：规格列表、SKU编码、SKU状态 */
    SKU,

    /** 品牌数据：品牌名称、品牌Logo */
    BRAND,

    /** 类目数据：类目ID、类目名称、层级路径 */
    CATEGORY,

    /** 图片数据：轮播图列表、详情图列表 */
    IMAGE,

    /** 扩展属性：EAV动态属性键值对 */
    ATTRIBUTE,

    /** 商品描述：富文本详情 */
    DESCRIPTION,

    /** 全部数据：返回以上所有维度 */
    ALL
}
