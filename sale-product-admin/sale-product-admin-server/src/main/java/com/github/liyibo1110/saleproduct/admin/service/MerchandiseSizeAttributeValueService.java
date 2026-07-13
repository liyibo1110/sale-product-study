package com.github.liyibo1110.saleproduct.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.liyibo1110.saleproduct.admin.entity.AttributeDefinition;
import com.github.liyibo1110.saleproduct.admin.entity.MerchandiseSizeAttributeValue;
import com.github.liyibo1110.saleproduct.admin.mapper.MerchandiseSizeAttributeValueMapper;
import com.github.liyibo1110.saleproduct.admin.vallidator.AttributeValidator;
import com.github.liyibo1110.saleproduct.base.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 尺码属性值服务。
 * 负责尺码维度的属性值写入，写入前会根据属性定义做校验。
 * @author liyibo
 * @date 2026-07-13 10:32
 */
@Service
public class MerchandiseSizeAttributeValueService {

    private final MerchandiseSizeAttributeValueMapper attrValueMapper;
    private final AttributeDefinitionService definitionService;
    private final AttributeValidator attributeValidator;

    public MerchandiseSizeAttributeValueService(MerchandiseSizeAttributeValueMapper attrValueMapper,
                                                AttributeDefinitionService definitionService,
                                                AttributeValidator attributeValidator) {
        this.attrValueMapper = attrValueMapper;
        this.definitionService = definitionService;
        this.attributeValidator = attributeValidator;
    }

    /**
     * 批量保存尺码属性值。
     * key = attr_key，value = 属性值字符串。
     */
    public void saveAttributes(Long sizeId, Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty())
            return;

        for (Map.Entry<String, String> entry : attributes.entrySet())
            saveOneAttribute(sizeId, entry.getKey(), entry.getValue());
    }

    /**
     * 保存单个尺码属性值。存在就更新，不存在就插入。
     */
    public void saveOneAttribute(Long sizeId, String attrKey, String attrValue) {
        // 查属性定义
        AttributeDefinition definition = definitionService.getByKey(attrKey);
        if (definition == null)
            throw new ValidationException("未知的属性: " + attrKey);

        // 用属性定义做校验
        attributeValidator.validate(definition, attrValue);

        // 查是否已存在
        MerchandiseSizeAttributeValue existing = attrValueMapper.selectOne(
                new LambdaQueryWrapper<MerchandiseSizeAttributeValue>()
                        .eq(MerchandiseSizeAttributeValue::getSizeId, sizeId)
                        .eq(MerchandiseSizeAttributeValue::getAttributeId, definition.getId())
                        .last("LIMIT 1"));

        if (existing != null) {
            existing.setAttrValue(attrValue);
            attrValueMapper.updateById(existing);
        } else {
            MerchandiseSizeAttributeValue newValue = new MerchandiseSizeAttributeValue();
            newValue.setSizeId(sizeId);
            newValue.setAttributeId(definition.getId());
            newValue.setAttrValue(attrValue);
            attrValueMapper.insert(newValue);
        }
    }

    /**
     * 查询某个尺码的所有属性值
     */
    public List<MerchandiseSizeAttributeValue> listBySizeId(Long sizeId) {
        return attrValueMapper.selectList(
                new LambdaQueryWrapper<MerchandiseSizeAttributeValue>()
                        .eq(MerchandiseSizeAttributeValue::getSizeId, sizeId));
    }
}
