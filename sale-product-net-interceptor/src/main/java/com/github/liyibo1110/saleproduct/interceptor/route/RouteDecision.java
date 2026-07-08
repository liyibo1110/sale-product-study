package com.github.liyibo1110.saleproduct.interceptor.route;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 一个请求到来，经过rule的各种判断，最终生成的判断结果封装。
 * @author liyibo
 * @date 2026-07-08 12:02
 */
@Data
@AllArgsConstructor
public class RouteDecision {

    private RouteTarget target;

    private String targetBaseUrl;

    private boolean collectEnabled;
}
