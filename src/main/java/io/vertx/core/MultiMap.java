package io.vertx.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class MultiMap extends HashMap<String, String> {
    private final boolean caseInsensitive;
    
    public MultiMap() {
        this(false);
    }
    
    public MultiMap(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }
    
    public static MultiMap caseInsensitiveMultiMap() {
        return new MultiMap(true);
    }

    public MultiMap set(String key, String value) {
        put(key, value);
        return this;
    }

    public boolean contains(String key) {
        if (caseInsensitive) {
            // 大小写不敏感查找
            for (String existingKey : keySet()) {
                if (existingKey.equalsIgnoreCase(key)) {
                    return true;
                }
            }
            return false;
        }
        return containsKey(key);
    }

    public String get(String key) {
        if (caseInsensitive) {
            // 大小写不敏感查找
            for (Map.Entry<String, String> entry : entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    return entry.getValue();
                }
            }
            return null;
        }
        return super.get(key);
    }

    public List<String> getAll(String key) {
        List<String> values = new ArrayList<>();
        if (containsKey(key)) {
            values.add(get(key));
        }
        return values;
    }

    public Map<String, String> toMap() {
        return new HashMap<>(this);
    }

    public void addAll(Map<String, String> map) {
        putAll(map);
    }

    public Set<String> names() {
        return keySet();
    }

    // add方法
    public MultiMap add(String key, String value) {
        put(key, value);
        return this;
    }
}
