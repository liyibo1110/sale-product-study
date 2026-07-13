package com.github.liyibo1110.saleproduct.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.liyibo1110.saleproduct.admin.entity.AttributeDefinition;
import com.github.liyibo1110.saleproduct.admin.mapper.AttributeDefinitionMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 属性定义服务。
 * 内存附带了本地缓存，避免每次校验都查数据库。
 * @author liyibo
 * @date 2026-07-13 10:15
 */
@Service
public class AttributeDefinitionService {

    private final AttributeDefinitionMapper definitionMapper;

    /**
     * attr_key -> AttributeDefinition 的本地缓存。
     * 属性定义变更频率极低，用ConcurrentHashMap缓存即可。
     */
    private final Map<String, AttributeDefinition> cache = new ConcurrentHashMap<>();

    public AttributeDefinitionService(AttributeDefinitionMapper definitionMapper) {
        this.definitionMapper = definitionMapper;
    }

    public AttributeDefinition getByKey(String attrKey) {
        AttributeDefinition cached = cache.get(attrKey);
        if (cached != null)
            return cached;

        // cache未命中再查数据库
        AttributeDefinition definition = definitionMapper.selectOne(
                new LambdaQueryWrapper<AttributeDefinition>()
                        .eq(AttributeDefinition::getAttrKey, attrKey)
                        .eq(AttributeDefinition::getStatus, 1)
                        .last("LIMIT 1"));
        if (definition != null)
            cache.put(attrKey, definition);

        return definition;
    }

    public AttributeDefinition getById(Long id) {
        return definitionMapper.selectById(id);
    }

    public List<AttributeDefinition> listEnabled() {
        return definitionMapper.selectList(
                new LambdaQueryWrapper<AttributeDefinition>()
                        .eq(AttributeDefinition::getStatus, 1)
                        .orderByAsc(AttributeDefinition::getSortOrder));
    }

    public void refreshCache() {
        cache.clear();
    }
}
