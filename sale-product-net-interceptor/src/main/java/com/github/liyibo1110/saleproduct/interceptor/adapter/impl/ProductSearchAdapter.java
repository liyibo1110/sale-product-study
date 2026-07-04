package com.github.liyibo1110.saleproduct.interceptor.adapter.impl;

import com.github.liyibo1110.saleproduct.interceptor.adapter.InterfaceAdapter;
import org.springframework.stereotype.Component;

/**
 * 产品搜索接口适配器实现。
 * @author liyibo
 * @date 2026-07-03 16:26
 */
@Component
public class ProductSearchAdapter implements InterfaceAdapter {

    @Override
    public String supportedUri() {
        return "/api/product/search";
    }

    @Override
    public byte[] convertRequest(byte[] phpRequestBody) {
        throw new UnsupportedOperationException("商品搜索接口的参数转换尚未实现");
    }

    @Override
    public byte[] convertResponse(byte[] javaResponseBody) {
        throw new UnsupportedOperationException("商品搜索接口的响应转换尚未实现");
    }
}
