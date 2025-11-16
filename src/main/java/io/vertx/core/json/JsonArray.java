package io.vertx.core.json;

import java.util.ArrayList;
import java.util.List;

public class JsonArray {
    public final List<Object> list;

    public JsonArray() {
        this.list = new ArrayList<>();
    }

    public JsonArray(String json) {
        this.list = new ArrayList<>();
        if (json == null || json.trim().isEmpty() || json.trim().equals("[]")) {
            return;
        }
        try {
            JsonObject.parseJsonArray(json, this);
        } catch (Exception e) {
            // Keep empty list on parse error
        }
    }

    public JsonArray(List<Object> list) {
        this.list = new ArrayList<>();
        if (list != null) {
            this.list.addAll(list);
        }
    }

    public JsonArray add(Object value) {
        list.add(value);
        return this;
    }

    public String getString(int index) {
        if (index < 0 || index >= list.size()) {
            return null;
        }
        Object value = list.get(index);
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return String.valueOf(value);
    }

    public Integer getInteger(int index) {
        if (index < 0 || index >= list.size()) {
            return null;
        }
        Object value = list.get(index);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public Long getLong(int index) {
        if (index < 0 || index >= list.size()) {
            return null;
        }
        Object value = list.get(index);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public Double getDouble(int index) {
        if (index < 0 || index >= list.size()) {
            return null;
        }
        Object value = list.get(index);
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.valueOf(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean getBoolean(int index) {
        if (index < 0 || index >= list.size()) {
            return null;
        }
        Object value = list.get(index);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public JsonObject getJsonObject(int index) {
        if (index < 0 || index >= list.size()) {
            return null;
        }
        Object value = list.get(index);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonObject) {
            return (JsonObject) value;
        }
        if (value instanceof java.util.Map) {
            JsonObject result = new JsonObject();
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> valueMap = (java.util.Map<String, Object>) value;
            result.map.putAll(valueMap);
            return result;
        }
        return null;
    }

    public JsonArray getJsonArray(int index) {
        if (index < 0 || index >= list.size()) {
            return null;
        }
        Object value = list.get(index);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonArray) {
            return (JsonArray) value;
        }
        if (value instanceof java.util.List) {
            JsonArray result = new JsonArray();
            @SuppressWarnings("unchecked")
            java.util.List<Object> valueList = (java.util.List<Object>) value;
            result.list.addAll(valueList);
            return result;
        }
        return null;
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public String encode() {
        return JsonObject.encodeJsonArray(this, 0, 0);
    }

    public String encodePrettily() {
        return JsonObject.encodeJsonArray(this, 2, 0);
    }

    public List<Object> getList() {
        return new ArrayList<>(list);
    }

    public void forEach(java.util.function.Consumer<Object> action) {
        list.forEach(action);
    }

    public static JsonArray of(Object value) {
        JsonArray array = new JsonArray();
        array.add(value);
        return array;
    }

    public static JsonArray of(Object value1, Object value2) {
        JsonArray array = new JsonArray();
        array.add(value1);
        array.add(value2);
        return array;
    }

    public static JsonArray of(Object value1, Object value2, Object value3) {
        JsonArray array = new JsonArray();
        array.add(value1);
        array.add(value2);
        array.add(value3);
        return array;
    }

    public static JsonArray of(Object value1, Object value2, Object value3, Object value4) {
        JsonArray array = new JsonArray();
        array.add(value1);
        array.add(value2);
        array.add(value3);
        array.add(value4);
        return array;
    }

    @Override
    public String toString() {
        return encode();
    }
}