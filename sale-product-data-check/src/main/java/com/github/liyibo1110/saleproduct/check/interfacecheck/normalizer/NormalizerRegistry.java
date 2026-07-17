package com.github.liyibo1110.saleproduct.check.interfacecheck.normalizer;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * FieldNormalizer实现类的存储库。
 * @author liyibo
 * @date 2026-07-16 14:27
 */
@Component
public class NormalizerRegistry {

    private final Map<String, FieldNormalizer> normalizers = new HashMap<>();

    @PostConstruct
    public void init() {
        normalizers.put("price", new PriceNormalizer());
        normalizers.put("integer", new IntegerNormalizer());
        normalizers.put("long", new LongNormalizer());
        normalizers.put("boolean", new BooleanNormalizer());
        normalizers.put("trim", new TrimNormalizer());
    }

    /**
     * 查找对应的归化组件，并尝试进行直接归化。
     */
    public String normalize(String normalizerName, String value) {
        if (value == null)
            return null;

        FieldNormalizer normalizer = normalizers.get(normalizerName);
        if (normalizer == null)
            return value;

        return normalizer.normalize(value);
    }
}
