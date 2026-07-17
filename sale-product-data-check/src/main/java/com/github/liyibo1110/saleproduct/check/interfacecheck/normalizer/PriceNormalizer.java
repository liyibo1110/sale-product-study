package com.github.liyibo1110.saleproduct.check.interfacecheck.normalizer;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author liyibo
 * @date 2026-07-16 14:27
 */
public class PriceNormalizer implements FieldNormalizer {

    @Override
    public String normalize(String value) {
        if (value == null || value.isEmpty())
            return "0";

        try {
            // 尝试按小数解析（PHP的格式）
            if (value.contains(".")) {
                BigDecimal decimal = new BigDecimal(value);
                return decimal.multiply(BigDecimal.valueOf(100))
                        .setScale(0, RoundingMode.HALF_UP)
                        .toPlainString();
            }
            // 已经是整数（Java的格式，单位是分）
            return value;
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
