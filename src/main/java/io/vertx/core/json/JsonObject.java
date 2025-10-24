package io.vertx.core.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public class JsonObject {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectNode node;

    public JsonObject() {
        this.node = mapper.createObjectNode();
    }

    public JsonObject(String json) {
        ObjectNode tempNode;
        try {
            JsonNode jsonNode = mapper.readTree(json);
            if (jsonNode.isObject()) {
                tempNode = (ObjectNode) jsonNode;
            } else {
                tempNode = mapper.createObjectNode();
            }
        } catch (Exception e) {
            tempNode = mapper.createObjectNode();
        }
        this.node = tempNode;
    }

    public JsonObject(Map<String, Object> map) {
        this.node = mapper.createObjectNode();
        if (map != null) {
            map.forEach(this::put);
        }
    }

    public JsonObject put(String key, Object value) {
        if (value == null) {
            node.putNull(key);
        } else if (value instanceof String) {
            node.put(key, (String) value);
        } else if (value instanceof Integer) {
            node.put(key, (Integer) value);
        } else if (value instanceof Long) {
            node.put(key, (Long) value);
        } else if (value instanceof Double) {
            node.put(key, (Double) value);
        } else if (value instanceof Boolean) {
            node.put(key, (Boolean) value);
        } else {
            node.putPOJO(key, value);
        }
        return this;
    }

    public String getString(String key) {
        JsonNode value = node.get(key);
        return value != null && value.isTextual() ? value.asText() : null;
    }

    public String getString(String key, String defaultValue) {
        JsonNode value = node.get(key);
        if (value != null && value.isTextual()) {
            return value.asText();
        }
        return defaultValue;
    }

    public Integer getInteger(String key) {
        JsonNode value = node.get(key);
        return value != null && value.isInt() ? value.asInt() : null;
    }

    public Long getLong(String key) {
        JsonNode value = node.get(key);
        return value != null && value.isNumber() ? value.asLong() : null;
    }

    public Double getDouble(String key) {
        JsonNode value = node.get(key);
        return value != null && value.isNumber() ? value.asDouble() : null;
    }

    public Boolean getBoolean(String key) {
        JsonNode value = node.get(key);
        return value != null && value.isBoolean() ? value.asBoolean() : null;
    }

    public JsonObject getJsonObject(String key) {
        JsonNode value = node.get(key);
        if (value != null && value.isObject()) {
            JsonObject result = new JsonObject();
            result.node.setAll((ObjectNode) value);
            return result;
        }
        return null;
    }

    public JsonArray getJsonArray(String key) {
        JsonNode value = node.get(key);
        if (value != null && value.isArray()) {
            JsonArray result = new JsonArray();
            // Use reflection to access the node field
            try {
                java.lang.reflect.Field field = JsonArray.class.getDeclaredField("node");
                field.setAccessible(true);
                Object resultNode = field.get(result);
                ((com.fasterxml.jackson.databind.node.ArrayNode) resultNode).addAll((com.fasterxml.jackson.databind.node.ArrayNode) value);
            } catch (Exception e) {
                result.add(mapper.convertValue(value, Object.class));
            }
            return result;
        }
        return null;
    }

    public boolean containsKey(String key) {
        return node.has(key);
    }

    public String encode() {
        try {
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }

    public String encodePrettily() {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }

    public static JsonObject of(String key1, Object value1) {
        return new JsonObject().put(key1, value1);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2) {
        return new JsonObject().put(key1, value1).put(key2, value2);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3).put(key4, value4);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3).put(key4, value4).put(key5, value5);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5, String key6, Object value6) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3).put(key4, value4).put(key5, value5).put(key6, value6);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5, String key6, Object value6, String key7, Object value7) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3).put(key4, value4).put(key5, value5).put(key6, value6).put(key7, value7);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5, String key6, Object value6, String key7, Object value7, String key8, Object value8) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3).put(key4, value4).put(key5, value5).put(key6, value6).put(key7, value7).put(key8, value8);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5, String key6, Object value6, String key7, Object value7, String key8, Object value8, String key9, Object value9) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3).put(key4, value4).put(key5, value5).put(key6, value6).put(key7, value7).put(key8, value8).put(key9, value9);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5, String key6, Object value6, String key7, Object value7, String key8, Object value8, String key9, Object value9, String key10, Object value10) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3).put(key4, value4).put(key5, value5).put(key6, value6).put(key7, value7).put(key8, value8).put(key9, value9).put(key10, value10);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5, String key6, Object value6, String key7, Object value7, String key8, Object value8, String key9, Object value9, String key10, Object value10, String key11, Object value11) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3).put(key4, value4).put(key5, value5).put(key6, value6).put(key7, value7).put(key8, value8).put(key9, value9).put(key10, value10).put(key11, value11);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5, String key6, Object value6, String key7, Object value7, String key8, Object value8, String key9, Object value9, String key10, Object value10, String key11, Object value11, String key12, Object value12) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3).put(key4, value4).put(key5, value5).put(key6, value6).put(key7, value7).put(key8, value8).put(key9, value9).put(key10, value10).put(key11, value11).put(key12, value12);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5, String key6, Object value6, String key7, Object value7, String key8, Object value8, String key9, Object value9, String key10, Object value10, String key11, Object value11, String key12, Object value12, String key13, Object value13) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3).put(key4, value4).put(key5, value5).put(key6, value6).put(key7, value7).put(key8, value8).put(key9, value9).put(key10, value10).put(key11, value11).put(key12, value12).put(key13, value13);
    }

    public static JsonObject of(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4, String key5, Object value5, String key6, Object value6, String key7, Object value7, String key8, Object value8, String key9, Object value9, String key10, Object value10, String key11, Object value11, String key12, Object value12, String key13, Object value13, String key14, Object value14) {
        return new JsonObject().put(key1, value1).put(key2, value2).put(key3, value3).put(key4, value4).put(key5, value5).put(key6, value6).put(key7, value7).put(key8, value8).put(key9, value9).put(key10, value10).put(key11, value11).put(key12, value12).put(key13, value13).put(key14, value14);
    }

    public static JsonObject of() {
        return new JsonObject();
    }

    public boolean isEmpty() {
        return node.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap() {
        return mapper.convertValue(node, Map.class);
    }

    @Override
    public String toString() {
        return encode();
    }
}
