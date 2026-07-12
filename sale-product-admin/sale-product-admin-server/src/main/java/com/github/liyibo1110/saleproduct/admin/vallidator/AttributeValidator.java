package com.github.liyibo1110.saleproduct.admin.vallidator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.saleproduct.admin.entity.AttributeDefinition;
import com.github.liyibo1110.saleproduct.admin.enums.AttrType;
import com.github.liyibo1110.saleproduct.base.exception.ValidationException;

import java.math.BigDecimal;
import java.util.List;

/**
 * EAV属性值校验器。
 * 根据属性定义表中的元数据，对入参值做类型校验和约束校验。
 * @author liyibo
 * @date 2026-07-11 13:36
 */
public class AttributeValidator {

    private final ObjectMapper objectMapper;

    public AttributeValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 校验属性值是否符合属性定义的约束。
     * @param definition 属性定义（元数据）
     * @param value 入参值（所有类型统一用字符串传入）
     */
    public void validate(AttributeDefinition definition, String value) {
        String attrName = definition.getAttrName();

        // 必填校验
        if (definition.getRequired() != null && definition.getRequired() == 1) {
            if (value == null || value.isEmpty()) {
                throw new ValidationException("属性[" + attrName + "]不能为空");
            }
        }

        // 非必填且值为空，跳过后续类型校验
        if (value == null || value.isEmpty())
            return;

        AttrType attrType = AttrType.of(definition.getAttrType());
        switch (attrType) {
            case STRING -> validateString(definition, value);
            case INTEGER -> validateInteger(definition, value);
            case DECIMAL -> validateDecimal(definition, value);
            case BOOLEAN -> validateBoolean(definition, value);
            case ENUM -> validateEnum(definition, value);
            case JSON -> validateJson(definition, value);
        }
    }

    /**
     * 里面只有验证字符串长度。
     */
    private void validateString(AttributeDefinition def, String value) {
        if (def.getMaxLength() != null && value.length() > def.getMaxLength())
            throw new ValidationException("属性[" + def.getAttrName() + "]长度不能超过" + def.getMaxLength());
    }

    /**
     * 验证是否为整数，以及是否超出范围。
     */
    private void validateInteger(AttributeDefinition def, String value) {
        long parsed;
        try {
            parsed = Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ValidationException("属性[" + def.getAttrName() + "]要求整数，实际值: " + value);
        }
        checkRange(def, BigDecimal.valueOf(parsed));
    }

    /**
     * 验证是否为定点数，以及是否超出范围。
     */
    private void validateDecimal(AttributeDefinition def, String value) {
        BigDecimal parsed;
        try {
            parsed = new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new ValidationException("属性[" + def.getAttrName() + "]要求小数，实际值: " + value);
        }
        checkRange(def, parsed);
    }

    /**
     * 验证是否为true或false。
     */
    private void validateBoolean(AttributeDefinition def, String value) {
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value))
            throw new ValidationException("属性[" + def.getAttrName() + "]要求布尔值(true/false)，实际值: " + value);
    }

    /**
     * 验证是否为JSON数组格式的字符串。
     */
    private void validateEnum(AttributeDefinition def, String value) {
        if (def.getEnumValues() == null || def.getEnumValues().isEmpty())
            return;

        List<String> allowedValues;
        try {
            allowedValues = objectMapper.readValue(def.getEnumValues(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new ValidationException("属性[" + def.getAttrName() + "]的枚举值配置格式错误");
        }
        if (!allowedValues.contains(value))
            throw new ValidationException("属性[" + def.getAttrName() + "]的值不在允许范围内，" + "允许值: " + allowedValues + "，实际值: " + value);
    }

    /**
     * 验证是否为JSON格式的字符串。
     */
    private void validateJson(AttributeDefinition def, String value) {
        try {
            objectMapper.readTree(value);
        } catch (JsonProcessingException e) {
            throw new ValidationException("属性[" + def.getAttrName() + "]要求JSON格式，实际值不是合法JSON");
        }
    }

    /**
     * 验证定点数是否超出范围
     */
    private void checkRange(AttributeDefinition def, BigDecimal value) {
        if (def.getMinValue() != null && value.compareTo(def.getMinValue()) < 0)
            throw new ValidationException("属性[" + def.getAttrName() + "]的值不能小于" + def.getMinValue());

        if (def.getMaxValue() != null && value.compareTo(def.getMaxValue()) > 0)
            throw new ValidationException("属性[" + def.getAttrName() + "]的值不能大于" + def.getMaxValue());
    }
}
