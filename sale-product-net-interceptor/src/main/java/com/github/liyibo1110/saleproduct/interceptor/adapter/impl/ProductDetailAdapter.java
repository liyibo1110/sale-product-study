package com.github.liyibo1110.saleproduct.interceptor.adapter.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.liyibo1110.saleproduct.interceptor.adapter.InterfaceAdapter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 产品详情接口适配器实现。
 * @author liyibo
 * @date 2026-07-03 16:24
 */
@Component
public class ProductDetailAdapter implements InterfaceAdapter {

    private final ObjectMapper objectMapper;

    private static final Map<String, Integer> STATUS_MAPPING = Map.of(
        "ON_SALE", 1,
        "OFF_SALE", 0,
        "PRE_SALE", 2,
        "SOLD_OUT", 3
    );

    public ProductDetailAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String supportedUri() {
        return "/api/product/detail";
    }

    @Override
    public byte[] convertRequest(byte[] phpRequestBody) {
        try {
            JsonNode phpNode = objectMapper.readTree(phpRequestBody);
            ObjectNode javaNode = objectMapper.createObjectNode();

            // product_id → productId
            if (phpNode.has("product_id"))
                javaNode.set("productId", phpNode.get("product_id"));

            // channel 转大写
            if (phpNode.has("channel"))
                javaNode.put("channel", phpNode.get("channel").asText().toUpperCase());

            // include_* 标记转换为options数组
            ArrayNode options = objectMapper.createArrayNode();
            // BASE和PRICE始终包含
            options.add("BASE");
            options.add("PRICE");
            addOptionIfEnabled(phpNode, "include_sku", "SKU", options);
            addOptionIfEnabled(phpNode, "include_brand", "BRAND", options);
            addOptionIfEnabled(phpNode, "include_category", "CATEGORY", options);
            addOptionIfEnabled(phpNode, "include_images", "IMAGE", options);
            addOptionIfEnabled(phpNode, "include_desc", "DESCRIPTION", options);
            javaNode.set("options", options);

            return objectMapper.writeValueAsBytes(javaNode);
        } catch (Exception e) {
            throw new RuntimeException("商品详情请求参数转换失败", e);
        }
    }

    @Override
    public byte[] convertResponse(byte[] javaResponseBody) {
        try {
            JsonNode javaRoot = objectMapper.readTree(javaResponseBody);
            ObjectNode phpRoot = objectMapper.createObjectNode();

            // 包装层转换
            phpRoot.put("code", 0);
            phpRoot.put("message", "success");

            JsonNode data = javaRoot.get("data");
            if (data == null || data.isNull()) {
                phpRoot.putNull("data");
                return objectMapper.writeValueAsBytes(phpRoot);
            }

            ObjectNode phpData = objectMapper.createObjectNode();

            // 基础字段 camelCase → snake_case
            copyField(data, "productId", phpData, "product_id");
            copyField(data, "productName", phpData, "product_name");
            copyField(data, "mainImage", phpData, "main_image");

            // status枚举转整数
            if (data.has("status")) {
                String statusStr = data.get("status").asText();
                phpData.put("status", STATUS_MAPPING.getOrDefault(statusStr, -1));
            }

            // price嵌套结构展平，分转元
            JsonNode price = data.get("price");
            if (price != null && !price.isNull()) {
                phpData.put("price", centsToYuan(price, "salePrice"));
                phpData.put("original_price", centsToYuan(price, "originalPrice"));
            }

            // brand嵌套结构展平
            JsonNode brand = data.get("brand");
            if (brand != null && !brand.isNull()) {
                copyField(brand, "brandName", phpData, "brand_name");
                copyField(brand, "brandLogo", phpData, "brand_logo");
            }

            // skuList数组转换
            JsonNode skuList = data.get("skuList");
            if (skuList != null && skuList.isArray()) {
                ArrayNode phpSkuList = objectMapper.createArrayNode();
                for (JsonNode sku : skuList) {
                    ObjectNode phpSku = objectMapper.createObjectNode();
                    copyField(sku, "skuId", phpSku, "sku_id");
                    copyField(sku, "skuCode", phpSku, "sku_code");
                    copyField(sku, "skuName", phpSku, "sku_name");
                    if (sku.has("status")) {
                        String skuStatus = sku.get("status").asText();
                        phpSku.put("status", STATUS_MAPPING.getOrDefault(skuStatus, -1));
                    }
                    if (sku.has("price")) {
                        JsonNode skuPrice = sku.get("price");
                        phpSku.put("price", centsToYuan(skuPrice, "salePrice"));
                    }
                    phpSkuList.add(phpSku);
                }
                phpData.set("sku_list", phpSkuList);
            }

            // category展平
            JsonNode category = data.get("category");
            if (category != null && !category.isNull()) {
                copyField(category, "categoryId", phpData, "category_id");
                copyField(category, "categoryName", phpData, "category_name");
            }

            phpRoot.set("data", phpData);
            return objectMapper.writeValueAsBytes(phpRoot);
        } catch (Exception e) {
            throw new RuntimeException("商品详情响应转换失败", e);
        }
    }

    private void addOptionIfEnabled(JsonNode node, String field, String option, ArrayNode options) {
        if (node.has(field) && node.get(field).asInt(0) == 1)
            options.add(option);
    }

    private void copyField(JsonNode source, String sourceField, ObjectNode target, String targetField) {
        if (source.has(sourceField))
            target.set(targetField, source.get(sourceField));
    }

    private String centsToYuan(JsonNode parent, String field) {
        if (!parent.has(field))
            return "0.00";

        long cents = parent.get(field).asLong(0);
        return BigDecimal.valueOf(cents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .toPlainString();
    }
}
