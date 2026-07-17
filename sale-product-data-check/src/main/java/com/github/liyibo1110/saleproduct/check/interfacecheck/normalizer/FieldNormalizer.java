package com.github.liyibo1110.saleproduct.check.interfacecheck.normalizer;

/**
 * 归一化方法，即将接收的String字段值（可能是php版本，也可能是Java版本的）进行统一的新版String类型转换。
 * @author liyibo
 * @date 2026-07-16 14:17
 */
public interface FieldNormalizer {

    String normalize(String value);
}
