package com.github.liyibo1110.saleproduct.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.liyibo1110.saleproduct.admin.entity.AttributeDefinition;
import com.github.liyibo1110.saleproduct.admin.entity.MerchandiseAttributeValue;
import com.github.liyibo1110.saleproduct.admin.mapper.MerchandiseAttributeValueMapper;
import com.github.liyibo1110.saleproduct.admin.vallidator.AttributeValidator;
import com.github.liyibo1110.saleproduct.base.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 商品属性值服务。
 * 负责商品维度的属性值写入，写入前会根据属性定义做校验。
 * @author liyibo
 * @date 2026-07-13 10:31
 */
@Service
public class MerchandiseAttributeValueService {

    private final MerchandiseAttributeValueMapper attrValueMapper;
    private final AttributeDefinitionService definitionService;
    private final AttributeValidator attributeValidator;

    public MerchandiseAttributeValueService(MerchandiseAttributeValueMapper attrValueMapper,
                                            AttributeDefinitionService definitionService,
                                            AttributeValidator attributeValidator) {
        this.attrValueMapper = attrValueMapper;
        this.definitionService = definitionService;
        this.attributeValidator = attributeValidator;
    }

    /**
     * 批量保存属性值。
     * key = attr_key，value = 属性值字符串。
     */
    public void saveAttributes(Long merchandiseId, Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty())
            return;

        for (Map.Entry<String, String> entry : attributes.entrySet())
            saveOneAttribute(merchandiseId, entry.getKey(), entry.getValue());
    }

    /**
     * 保存单个属性值。存在就更新，不存在就插入。
     */
    public void saveOneAttribute(Long merchandiseId, String attrKey, String attrValue) {
        // 查属性定义
        AttributeDefinition definition = definitionService.getByKey(attrKey);
        if (definition == null) {
            throw new ValidationException("未知的属性: " + attrKey);
        }

        // 用属性定义做校验
        attributeValidator.validate(definition, attrValue);

        // 查是否已存在
        MerchandiseAttributeValue existing = attrValueMapper.selectOne(
                new LambdaQueryWrapper<MerchandiseAttributeValue>()
                        .eq(MerchandiseAttributeValue::getMerchandiseId, merchandiseId)
                        .eq(MerchandiseAttributeValue::getAttributeId, definition.getId())
                        .last("LIMIT 1"));

        if (existing != null) {
            existing.setAttrValue(attrValue);
            attrValueMapper.updateById(existing);
        } else {
            MerchandiseAttributeValue newValue = new MerchandiseAttributeValue();
            newValue.setMerchandiseId(merchandiseId);
            newValue.setAttributeId(definition.getId());
            newValue.setAttrValue(attrValue);
            attrValueMapper.insert(newValue);
        }
    }

    /**
     * 查询某个商品的所有属性值
     */
    public List<MerchandiseAttributeValue> listByMerchandiseId(Long merchandiseId) {
        return attrValueMapper.selectList(
                new LambdaQueryWrapper<MerchandiseAttributeValue>()
                        .eq(MerchandiseAttributeValue::getMerchandiseId, merchandiseId));
    }
}
