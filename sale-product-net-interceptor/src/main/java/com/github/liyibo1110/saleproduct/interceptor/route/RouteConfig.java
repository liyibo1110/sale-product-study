package com.github.liyibo1110.saleproduct.interceptor.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 转发路由整体规则，规则在RuleItem，配置值来自于Nacos Config。
 * @author liyibo
 * @date 2026-07-08 11:53
 */
@Data
public class RouteConfig {

    @JsonProperty("defaultTarget")
    private String defaultTarget;

    @JsonProperty("phpBaseUrl")
    private String phpBaseUrl;

    @JsonProperty("javaBaseUrl")
    private String javaBaseUrl;

    @JsonProperty("rules")
    private List<RuleItem> rules;
}
