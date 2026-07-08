package com.github.liyibo1110.saleproduct.interceptor.route;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 路由决策管理器
 * @author liyibo
 * @date 2026-07-08 11:43
 */
@Component
public class RouteRuleManager {

    private static final Logger log = LoggerFactory.getLogger(RouteRuleManager.class);

    private static final String DATA_ID = "traffic-hijack-route-config";
    private static final String GROUP = "PRODUCT_SYSTEM";

    private final NacosConfigManager nacosConfigManager;
    private final ObjectMapper objectMapper;

    private volatile RouteConfig routeConfig;

    public RouteRuleManager(NacosConfigManager nacosConfigManager, ObjectMapper objectMapper) {
        this.nacosConfigManager = nacosConfigManager;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        try {
            ConfigService configService = nacosConfigManager.getConfigService();
            // 加载初始配置
            String configJson = configService.getConfig(DATA_ID, GROUP, 5000);
            refreshConfig(configJson);
            // 注册配置变更监听器，Nacos推送后秒级生效
            configService.addListener(DATA_ID, GROUP, new AbstractListener() {
                @Override
                public void receiveConfigInfo(String configJson) {
                    refreshConfig(configJson);
                }
            });
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("路由配置初始化失败，所有请求默认走PHP")
                    .exception(e)
                    .log();
        }
    }

    private void refreshConfig(String configJson) {
        if (configJson == null || configJson.isBlank())
            return;
        try {
            this.routeConfig = objectMapper.readValue(configJson, RouteConfig.class);
            StructuredLog.info(log)
                    .message("路由配置更新")
                    .put("config", configJson)
                    .log();
        } catch (Exception e) {
            StructuredLog.error(log)
                    .message("路由配置解析失败")
                    .put("configJson", configJson)
                    .exception(e)
                    .log();
        }
    }

    /**
     * 核心方法：对给定的uri进行转发规则判断，最终返回封装好的判断结果。
     */
    public RouteDecision resolve(String uri) {
        RouteConfig config = this.routeConfig;
        // Nacos没有配置，就转发给默认版本的php
        if (config == null || config.getRules() == null)
            return defaultPhp(config);

        // 依照规则来判断
        for (RuleItem item : config.getRules()) {
            if (uri.startsWith(item.getUriPattern()))
                return resolveByRule(config, item);
        }

        // 没有匹配到任何规则，就转发给默认Target
        return defaultTarget(config);
    }

    /**
     * 已经匹配到了uri，这里是根据灰度比例，来决定是随机给php还是给Java。
     */
    private RouteDecision resolveByRule(RouteConfig config, RuleItem item) {
        boolean toJava = ThreadLocalRandom.current().nextInt(100) < item.getJavaPercent();
        if (toJava)
            return new RouteDecision(RouteTarget.JAVA, config.getJavaBaseUrl(), false); // 转发Java不需要采样检测
        return new RouteDecision(RouteTarget.PHP, config.getPhpBaseUrl(), item.isCollectEnabled()); // 转发php可能需要采样检测
    }

    /**
     * 构造默认php的RouteDecision，同时不开启采样检测。
     */
    private RouteDecision defaultPhp(RouteConfig config) {
        String phpUrl = config != null ? config.getPhpBaseUrl() : "http://localhost:8080";
        return new RouteDecision(RouteTarget.PHP, phpUrl, false);
    }

    /**
     * 构造默认RouteDecision，如果配置指定了Java就转发Java，否则就是转发php，同时都不开启采样检测。
     */
    private RouteDecision defaultTarget(RouteConfig config) {
        if ("java".equalsIgnoreCase(config.getDefaultTarget()))
            return new RouteDecision(RouteTarget.JAVA, config.getJavaBaseUrl(), false);

        return new RouteDecision(RouteTarget.PHP, config.getPhpBaseUrl(), false);
    }
}
