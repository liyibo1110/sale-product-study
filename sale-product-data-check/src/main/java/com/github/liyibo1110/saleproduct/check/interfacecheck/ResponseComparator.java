package com.github.liyibo1110.saleproduct.check.interfacecheck;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.saleproduct.base.log.StructuredLog;
import com.github.liyibo1110.saleproduct.check.interfacecheck.normalizer.NormalizerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author liyibo
 * @date 2026-07-16 15:01
 */
@Component
public class ResponseComparator {

    private static final Logger log = LoggerFactory.getLogger(ResponseComparator.class);

    private final ObjectMapper objectMapper;
    private final FieldMappingConfig fieldMappingConfig;
    private final NormalizerRegistry normalizerRegistry;

    public ResponseComparator(ObjectMapper objectMapper,
                              FieldMappingConfig fieldMappingConfig,
                              NormalizerRegistry normalizerRegistry) {
        this.objectMapper = objectMapper;
        this.fieldMappingConfig = fieldMappingConfig;
        this.normalizerRegistry = normalizerRegistry;
    }

    /**
     * 比较特定请求的php响应与Java响应的检验差异
     */
    public List<FieldDiff> compare(String uri, byte[] phpResponse, byte[] javaResponse) {
        List<FieldDiff> diffs = new ArrayList<>();

        JsonNode phpJson = parseJson(phpResponse);
        JsonNode javaJson = parseJson(javaResponse);
        if (phpJson == null || javaJson == null)
            return diffs;

        // 提取两边响应中的业务数据节点
        JsonNode phpData = extractData(phpJson);
        JsonNode javaData = extractData(javaJson);
        if (phpData == null || javaData == null)
            return diffs;

        // 按字段映射配置逐字段比对
        List<FieldMapping> mappings = fieldMappingConfig.getMappings(uri);

        // 判断是否是列表接口
        if (phpData.isArray() && javaData.isArray()) {
            compareList(phpData, javaData, mappings, diffs);
        } else {
            for (FieldMapping mapping : mappings) {
                compareSingleField(phpData, javaData, mapping, diffs);
            }
        }

        return diffs;
    }

    private void compareList(JsonNode phpArray, JsonNode javaArray,
                             List<FieldMapping> mappings, List<FieldDiff> diffs) {
        // 用商品编码做关联键
        Map<String, JsonNode> phpMap = buildCodeMap(phpArray, "product_code");
        Map<String, JsonNode> javaMap = buildCodeMap(javaArray, "merchandiseCode");

        // 再比较单个对象
        for (Map.Entry<String, JsonNode> entry : phpMap.entrySet()) {
            String code = entry.getKey();
            JsonNode phpItem = entry.getValue();
            JsonNode javaItem = javaMap.get(code);

            if (javaItem == null) {
                // PHP有但Java没有
                FieldDiff diff = new FieldDiff();
                diff.setPhpField("list_item");
                diff.setJavaField("list_item");
                diff.setPhpValue("exists (code=" + code + ")");
                diff.setJavaValue("missing");
                diffs.add(diff);
                continue;
            }

            // 逐字段比对单个商品
            for (FieldMapping mapping : mappings)
                compareSingleField(phpItem, javaItem, mapping, diffs);
        }
    }

    /**
     * 比较单个字段的值
     */
    private void compareSingleField(JsonNode phpData, JsonNode javaData,
                                    FieldMapping mapping, List<FieldDiff> diffs) {
        String phpFieldPath = mapping.getPhpField();
        String javaFieldPath = mapping.getJavaField();
        String normalizer = mapping.getNormalizer();

        // 去掉列表接口的[*]前缀
        phpFieldPath = stripArrayPrefix(phpFieldPath);
        javaFieldPath = stripArrayPrefix(javaFieldPath);

        String phpValue = extractFieldValue(phpData, phpFieldPath);
        String javaValue = extractFieldValue(javaData, javaFieldPath);

        // 归一化处理
        if (normalizer != null) {
            phpValue = normalizerRegistry.normalize(normalizer, phpValue);
            javaValue = normalizerRegistry.normalize(normalizer, javaValue);
        }

        // 比对，如果不一致则记录差异问题
        if (!Objects.equals(phpValue, javaValue)) {
            FieldDiff diff = new FieldDiff();
            diff.setPhpField(mapping.getPhpField());
            diff.setJavaField(mapping.getJavaField());
            diff.setPhpValue(truncate(phpValue, 500));
            diff.setJavaValue(truncate(javaValue, 500));
            diffs.add(diff);
        }
    }

    private String stripArrayPrefix(String fieldPath) {
        // 去掉 "data[*]." 前缀中的 [*] 部分
        if (fieldPath.contains("[*].")) {
            int idx = fieldPath.indexOf("[*].");
            return fieldPath.substring(idx + 4);
        }
        // 去掉 "data." 前缀
        if (fieldPath.startsWith("data."))
            return fieldPath.substring(5);

        return fieldPath;
    }

    private Map<String, JsonNode> buildCodeMap(JsonNode array, String codeField) {
        Map<String, JsonNode> map = new HashMap<>();
        for (JsonNode item : array) {
            JsonNode codeNode = item.get(codeField);
            if (codeNode != null && !codeNode.isNull()) {
                map.put(codeNode.asText(), item);
            }
        }
        return map;
    }

    private JsonNode parseJson(byte[] data) {
        if (data == null || data.length == 0)
            return null;

        try {
            return objectMapper.readTree(data);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("JSON解析失败")
                    .put("dataLength", data.length)
                    .exception(e)
                    .log();
            return null;
        }
    }

    private JsonNode extractData(JsonNode root) {
        // 响应格式: {"code":0, "data": {...}} 或 {"code":0, "data": [...]}
        if (root.has("data"))
            return root.get("data");

        return root;
    }

    /**
     * 字段路径支持嵌套访问（用点号分隔）和数组遍历（用[*]表示遍历数组中每个元素）。
     * 例如data.product_name表示响应JSON根节点下data对象的product_name字段。
     * 而data[*].product_name表示data数组中每个元素的product_name字段。
     */
    private String extractFieldValue(JsonNode node, String fieldPath) {
        if (node == null || fieldPath == null)
            return null;

        String[] parts = fieldPath.split("\\.");
        JsonNode current = node;

        for (String part : parts) {
            if (current == null || current.isNull()) {
                return null;
            }
            current = current.get(part);
        }

        if (current == null || current.isNull())
            return null;

        return current.asText();
    }

    private String truncate(String value, int maxLength) {
        if (value == null)
            return null;

        if (value.length() <= maxLength)
            return value;

        return value.substring(0, maxLength) + "...";
    }
}
