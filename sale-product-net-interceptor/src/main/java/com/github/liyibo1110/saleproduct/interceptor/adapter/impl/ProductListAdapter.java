package com.github.liyibo1110.saleproduct.interceptor.adapter.impl;

import com.github.liyibo1110.saleproduct.interceptor.adapter.InterfaceAdapter;
import org.springframework.stereotype.Component;

/**
 * 产品列表接口适配器实现。
 * @author liyibo
 * @date 2026-07-03 16:23
 */
@Component
public class ProductListAdapter implements InterfaceAdapter {

    @Override
    public String supportedUri() {
        return "/api/product/list";
    }

    @Override
    public byte[] convertRequest(byte[] phpRequestBody) {
        throw new UnsupportedOperationException("商品列表接口的参数转换尚未实现");
    }

    @Override
    public byte[] convertResponse(byte[] javaResponseBody) {
        throw new UnsupportedOperationException("商品列表接口的响应转换尚未实现");
    }
}
