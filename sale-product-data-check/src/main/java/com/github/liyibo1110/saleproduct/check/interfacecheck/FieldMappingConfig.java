package com.github.liyibo1110.saleproduct.check.interfacecheck;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liyibo
 * @date 2026-07-16 14:59
 */
@Component
@ConfigurationProperties(prefix = "interface-check")
@Data
public class FieldMappingConfig {

    private Map<String, List<FieldMapping>> fieldMappings = new HashMap<>();
    private List<String> ignoreFields = new ArrayList<>();

    public List<FieldMapping> getMappings(String uri) {
        return fieldMappings.getOrDefault(uri, new ArrayList<>());
    }
}
