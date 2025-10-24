package cn.qaiu.util;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaScript对象适配器
 * 简化JS对象的访问，将JS对象转为Map进行处理
 */
public class JsObjectMap {
    private final Map<String, Object> data;

    public JsObjectMap(Map<String, Object> data) {
        this.data = data != null ? data : new HashMap<>();
    }

    public static JsObjectMap from(Map<String, Object> map) {
        return new JsObjectMap(map);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public String getString(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    public Integer getInteger(String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return value != null ? Integer.parseInt(value.toString()) : null;
    }

    public Long getLong(String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return value != null ? Long.parseLong(value.toString()) : null;
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public Map<String, Object> toMap() {
        return new HashMap<>(data);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}

