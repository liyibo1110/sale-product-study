package com.github.liyibo1110.saleproduct.check.interfacecheck.normalizer;

/**
 * @author liyibo
 * @date 2026-07-16 14:23
 */
public class TrimNormalizer implements FieldNormalizer {

    @Override
    public String normalize(String value) {
        if (value == null)
            return null;

        return value.trim();
    }
}
