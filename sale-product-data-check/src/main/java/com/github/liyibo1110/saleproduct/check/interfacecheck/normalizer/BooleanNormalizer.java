package com.github.liyibo1110.saleproduct.check.interfacecheck.normalizer;

/**
 * @author liyibo
 * @date 2026-07-16 14:23
 */
public class BooleanNormalizer implements FieldNormalizer {

    @Override
    public String normalize(String value) {
        if (value == null || value.isEmpty())
            return null;

        // PHP用1/0表示布尔，Java用true/false
        if ("1".equals(value) || "true".equalsIgnoreCase(value))
            return "true";

        if ("0".equals(value) || "false".equalsIgnoreCase(value))
            return "false";

        return value;
    }
}
