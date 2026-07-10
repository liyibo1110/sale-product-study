package com.github.liyibo1110.saleproduct.interceptor.adapter;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * php接口 -> Java接口适配器的存储库。
 * @author liyibo
 * @date 2026-07-03 16:08
 */
@Component
public class AdapterRegistry {

    /** key是适配器里面的supportURI，value是适配器实现类 */
    private final Map<String, InterfaceAdapter> adapterMap = new HashMap<>();

    public AdapterRegistry(List<InterfaceAdapter> adapters) {
        for (InterfaceAdapter adapter : adapters)
            adapterMap.put(adapter.supportedUri(), adapter);
    }

    /**
     * 根据URI查找对应的适配器。
     * 匹配规则：URI以适配器的supportedUri开头即匹配。
     * 返回null表示没有注册适配器。
     */
    public InterfaceAdapter getAdapter(String uri) {
        // 先尝试精确匹配
        InterfaceAdapter adapter = adapterMap.get(uri);
        if (adapter != null)
            return adapter;

        // 再尝试前缀匹配
        for (Map.Entry<String, InterfaceAdapter> entry : adapterMap.entrySet()) {
            if (uri.startsWith(entry.getKey()))
                return entry.getValue();
        }
        return null;
    }
}
