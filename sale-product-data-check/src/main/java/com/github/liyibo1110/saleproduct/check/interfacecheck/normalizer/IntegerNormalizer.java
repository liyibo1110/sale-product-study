package com.github.liyibo1110.saleproduct.check.interfacecheck.normalizer;

import java.math.BigDecimal;

/**
 * @author liyibo
 * @date 2026-07-16 14:18
 */
public class IntegerNormalizer implements FieldNormalizer {

    @Override
    public String normalize(String value) {
        if (value == null || value.isEmpty())
            return null;

        try {
            // 去掉可能的小数部分和前导零
            long num = new BigDecimal(value).longValue();
            return String.valueOf(num);
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
