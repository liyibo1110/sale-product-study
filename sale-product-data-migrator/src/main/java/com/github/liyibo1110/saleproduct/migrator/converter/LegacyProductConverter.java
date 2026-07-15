package com.github.liyibo1110.saleproduct.migrator.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.saleproduct.admin.api.request.MerchandiseCreateRequest;
import com.github.liyibo1110.saleproduct.migrator.entity.LegacyProduct;
import com.github.liyibo1110.saleproduct.migrator.entity.LegacySku;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PHP旧表数据 → admin服务请求对象的转换器。
 * 负责字段映射、格式转换。数据原封不动迁移，不做截断处理。
 * @author liyibo
 * @date 2026-07-14 10:18
 */
@Component
public class LegacyProductConverter {

    private static final Logger log = LoggerFactory.getLogger(LegacyProductConverter.class);

    private final ObjectMapper objectMapper;

    public LegacyProductConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 将旧商品 + 旧SKU列表转换为MerchandiseCreateRequest
     */
    public MerchandiseCreateRequest convert(LegacyProduct legacy, List<LegacySku> skuList) {
        MerchandiseCreateRequest request = new MerchandiseCreateRequest();

        // 核心字段映射
        request.setMerchandiseName(legacy.getProductName());
        request.setMerchandiseCode(legacy.getProductCode());
        request.setCategoryId(legacy.getCategoryId());
        request.setBrandId(legacy.getBrandId());
        request.setMainImage(legacy.getMainImage());

        // 宽表列式属性 → EAV属性键值对
        request.setAttributes(convertAttributes(legacy));

        // SKU列表转换
        request.setSizeList(convertSkuList(skuList));

        return request;
    }

    /**
     * 宽表里的销售属性列 → EAV的Map<attrKey, attrValue>
     */
    private Map<String, String> convertAttributes(LegacyProduct legacy) {
        Map<String, String> attributes = new HashMap<>();

        // is_hot: 宽表TINYINT → EAV BOOLEAN字符串
        if (legacy.getIsHot() != null)
            attributes.put("is_hot", legacy.getIsHot() == 1 ? "true" : "false");

        // is_new
        if (legacy.getIsNew() != null)
            attributes.put("is_new", legacy.getIsNew() == 1 ? "true" : "false");

        // target_audience: 直接映射STRING类型
        if (legacy.getTargetAudience() != null && !legacy.getTargetAudience().isEmpty())
            attributes.put("target_audience", legacy.getTargetAudience());

        // network_type: ENUM类型
        if (legacy.getNetworkType() != null && !legacy.getNetworkType().isEmpty())
            attributes.put("network_type", legacy.getNetworkType());

        // origin: STRING类型
        if (legacy.getOrigin() != null && !legacy.getOrigin().isEmpty())
            attributes.put("origin", legacy.getOrigin());

        // warranty_months: INTEGER类型
        if (legacy.getWarrantyMonths() != null)
            attributes.put("warranty_months", String.valueOf(legacy.getWarrantyMonths()));

        // extra_info: JSON类型，需要验证合法性
        if (legacy.getExtraInfo() != null && !legacy.getExtraInfo().isEmpty()) {
            if (isValidJson(legacy.getExtraInfo())) {
                attributes.put("extra_info", legacy.getExtraInfo());
            }
        }

        return attributes;
    }

    private List<MerchandiseCreateRequest.SizeItem> convertSkuList(List<LegacySku> skuList) {
        if (skuList == null || skuList.isEmpty())
            return new ArrayList<>();

        List<MerchandiseCreateRequest.SizeItem> sizeItems = new ArrayList<>();
        for (LegacySku sku : skuList) {
            MerchandiseCreateRequest.SizeItem item = new MerchandiseCreateRequest.SizeItem();
            item.setSizeCode(sku.getSkuCode());
            item.setSizeName(sku.getSkuName());
            item.setPrice(sku.getPrice());
            item.setOriginalPrice(sku.getOriginalPrice());
            item.setSortOrder(sku.getSortOrder());

            // 拼装规格数据JSON
            item.setSpecData(buildSpecData(sku));

            // SKU维度的动态属性
            item.setSizeAttributes(convertSkuAttributes(sku));

            sizeItems.add(item);
        }
        return sizeItems;
    }

    /**
     * SKU的颜色和存储 → 规格JSON
     */
    private String buildSpecData(LegacySku sku) {
        Map<String, String> spec = new HashMap<>();
        if (sku.getColor() != null && !sku.getColor().isEmpty())
            spec.put("颜色", sku.getColor());

        if (sku.getStorage() != null && !sku.getStorage().isEmpty())
            spec.put("存储", sku.getStorage());

        try {
            return objectMapper.writeValueAsString(spec);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * SKU宽表列 → EAV尺码属性
     */
    private Map<String, String> convertSkuAttributes(LegacySku sku) {
        Map<String, String> attrs = new HashMap<>();
        if (sku.getColor() != null && !sku.getColor().isEmpty())
            attrs.put("color", sku.getColor());

        if (sku.getStorage() != null && !sku.getStorage().isEmpty())
            attrs.put("storage", sku.getStorage());

        if (sku.getWeight() != null)
            attrs.put("weight", String.valueOf(sku.getWeight()));

        return attrs;
    }

    private boolean isValidJson(String str) {
        try {
            objectMapper.readTree(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
