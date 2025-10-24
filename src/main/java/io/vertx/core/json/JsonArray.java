package io.vertx.core.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.ArrayList;
import java.util.List;

public class JsonArray {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ArrayNode node;

    public JsonArray() {
        this.node = mapper.createArrayNode();
    }

    public JsonArray(String json) {
        ArrayNode tempNode;
        try {
            JsonNode jsonNode = mapper.readTree(json);
            if (jsonNode.isArray()) {
                tempNode = (ArrayNode) jsonNode;
            } else {
                tempNode = mapper.createArrayNode();
            }
        } catch (Exception e) {
            tempNode = mapper.createArrayNode();
        }
        this.node = tempNode;
    }

    public JsonArray(List<Object> list) {
        this.node = mapper.createArrayNode();
        if (list != null) {
            list.forEach(this::add);
        }
    }

    public JsonArray add(Object value) {
        if (value == null) {
            node.addNull();
        } else if (value instanceof String) {
            node.add((String) value);
        } else if (value instanceof Integer) {
            node.add((Integer) value);
        } else if (value instanceof Long) {
            node.add((Long) value);
        } else if (value instanceof Double) {
            node.add((Double) value);
        } else if (value instanceof Boolean) {
            node.add((Boolean) value);
        } else if (value instanceof JsonObject) {
            // Use reflection to access the node field
            try {
                java.lang.reflect.Field field = JsonObject.class.getDeclaredField("node");
                field.setAccessible(true);
                Object jsonObjectNode = field.get(value);
                node.add((JsonNode) jsonObjectNode);
            } catch (Exception e) {
                node.addPOJO(value);
            }
        } else if (value instanceof JsonArray) {
            // Use reflection to access the node field
            try {
                java.lang.reflect.Field field = JsonArray.class.getDeclaredField("node");
                field.setAccessible(true);
                Object jsonArrayNode = field.get(value);
                node.add((JsonNode) jsonArrayNode);
            } catch (Exception e) {
                node.addPOJO(value);
            }
        } else {
            node.addPOJO(value);
        }
        return this;
    }

    public String getString(int index) {
        JsonNode value = node.get(index);
        return value != null && value.isTextual() ? value.asText() : null;
    }

    public Integer getInteger(int index) {
        JsonNode value = node.get(index);
        return value != null && value.isInt() ? value.asInt() : null;
    }

    public Long getLong(int index) {
        JsonNode value = node.get(index);
        return value != null && value.isNumber() ? value.asLong() : null;
    }

    public Double getDouble(int index) {
        JsonNode value = node.get(index);
        return value != null && value.isNumber() ? value.asDouble() : null;
    }

    public Boolean getBoolean(int index) {
        JsonNode value = node.get(index);
        return value != null && value.isBoolean() ? value.asBoolean() : null;
    }

    public JsonObject getJsonObject(int index) {
        JsonNode value = node.get(index);
        if (value != null && value.isObject()) {
            JsonObject result = new JsonObject();
            // Use reflection to access the node field
            try {
                java.lang.reflect.Field field = JsonObject.class.getDeclaredField("node");
                field.setAccessible(true);
                Object resultNode = field.get(result);
                ((com.fasterxml.jackson.databind.node.ObjectNode) resultNode).setAll((com.fasterxml.jackson.databind.node.ObjectNode) value);
            } catch (Exception e) {
                // Fallback to POJO conversion
                result.put("value", mapper.convertValue(value, Object.class));
            }
            return result;
        }
        return null;
    }

    public JsonArray getJsonArray(int index) {
        JsonNode value = node.get(index);
        if (value != null && value.isArray()) {
            JsonArray result = new JsonArray();
            // Use reflection to access the node field
            try {
                java.lang.reflect.Field field = JsonArray.class.getDeclaredField("node");
                field.setAccessible(true);
                Object resultNode = field.get(result);
                ((ArrayNode) resultNode).addAll((ArrayNode) value);
            } catch (Exception e) {
                // Fallback to POJO conversion
                result.add(mapper.convertValue(value, Object.class));
            }
            return result;
        }
        return null;
    }

    public int size() {
        return node.size();
    }

    public boolean isEmpty() {
        return node.isEmpty();
    }

    public String encode() {
        try {
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            return "[]";
        }
    }

    public String encodePrettily() {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            return "[]";
        }
    }

    public List<Object> getList() {
        List<Object> list = new ArrayList<>();
        for (JsonNode element : node) {
            if (element.isTextual()) {
                list.add(element.asText());
            } else if (element.isInt()) {
                list.add(element.asInt());
            } else if (element.isLong()) {
                list.add(element.asLong());
            } else if (element.isDouble()) {
                list.add(element.asDouble());
            } else if (element.isBoolean()) {
                list.add(element.asBoolean());
            } else if (element.isObject()) {
                JsonObject obj = new JsonObject();
                // Use reflection to access the node field
                try {
                    java.lang.reflect.Field field = JsonObject.class.getDeclaredField("node");
                    field.setAccessible(true);
                    Object objNode = field.get(obj);
                    ((com.fasterxml.jackson.databind.node.ObjectNode) objNode).setAll((com.fasterxml.jackson.databind.node.ObjectNode) element);
                } catch (Exception e) {
                    obj.put("value", mapper.convertValue(element, Object.class));
                }
                list.add(obj);
            } else if (element.isArray()) {
                JsonArray arr = new JsonArray();
                // Use reflection to access the node field
                try {
                    java.lang.reflect.Field field = JsonArray.class.getDeclaredField("node");
                    field.setAccessible(true);
                    Object arrNode = field.get(arr);
                    ((ArrayNode) arrNode).addAll((ArrayNode) element);
                } catch (Exception e) {
                    arr.add(mapper.convertValue(element, Object.class));
                }
                list.add(arr);
            } else {
                list.add(null);
            }
        }
        return list;
    }

    public void forEach(java.util.function.Consumer<Object> action) {
        List<Object> list = getList();
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