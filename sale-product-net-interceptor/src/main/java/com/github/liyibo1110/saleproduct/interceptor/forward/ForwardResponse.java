package com.github.liyibo1110.saleproduct.interceptor.forward;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * 转发HTTP响应的统一封装。
 * @author liyibo
 * @date 2026-07-06 13:19
 */
@Data
@AllArgsConstructor
public class ForwardResponse {

    private int statusCode;

    private Map<String, String> responseHeaders;

    private byte[] body;
}
