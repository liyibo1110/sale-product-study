package com.github.liyibo1110.saleproduct.interceptor.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 转发路由单个规则，主要封装了灰度转发比例，配置值来自于Nacos Config。
 * @author liyibo
 * @date 2026-07-06 13:10
 */
@Data
public class RuleItem {

    @JsonProperty("uriPattern")
    private String uriPattern;

    @JsonProperty("javaPercent")
    private int javaPercent;

    @JsonProperty("collectEnabled")
    private boolean collectEnabled;
}
