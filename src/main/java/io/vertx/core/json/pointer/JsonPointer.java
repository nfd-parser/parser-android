package io.vertx.core.json.pointer;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonPointer {
    private final String pointer;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonPointer(String pointer) {
        this.pointer = pointer;
    }

    public static JsonPointer of(String pointer) {
        return new JsonPointer(pointer);
    }

    public static JsonPointer from(String pointer) {
        return new JsonPointer(pointer);
    }

    public String queryFrom(JsonObject jsonObject) {
        try {
            JsonNode node = mapper.readTree(jsonObject.encode());
            JsonNode result = node.at(pointer);
            return result.isMissingNode() ? null : result.asText();
        } catch (Exception e) {
            return null;
        }
    }

    public String queryFrom(JsonArray jsonArray) {
        try {
            JsonNode node = mapper.readTree(jsonArray.encode());
            JsonNode result = node.at(pointer);
            return result.isMissingNode() ? null : result.asText();
        } catch (Exception e) {
            return null;
        }
    }

    public JsonNode queryNodeFrom(JsonObject jsonObject) {
        try {
            JsonNode node = mapper.readTree(jsonObject.encode());
            return node.at(pointer);
        } catch (Exception e) {
            return mapper.createObjectNode();
        }
    }

    public JsonNode queryNodeFrom(JsonArray jsonArray) {
        try {
            JsonNode node = mapper.readTree(jsonArray.encode());
            return node.at(pointer);
        } catch (Exception e) {
            return mapper.createArrayNode();
        }
    }

    public Object queryJson(JsonObject jsonObject) {
        try {
            JsonNode node = mapper.readTree(jsonObject.encode());
            JsonNode result = node.at(pointer);
            if (result.isMissingNode()) {
                return null;
            }
            if (result.isObject()) {
                JsonObject obj = new JsonObject();
                // Use reflection to access the node field
                try {
                    java.lang.reflect.Field field = JsonObject.class.getDeclaredField("node");
                    field.setAccessible(true);
                    Object objNode = field.get(obj);
                    ((com.fasterxml.jackson.databind.node.ObjectNode) objNode).setAll((com.fasterxml.jackson.databind.node.ObjectNode) result);
                } catch (Exception e) {
                    obj.put("value", mapper.convertValue(result, Object.class));
                }
                return obj;
            } else if (result.isArray()) {
                JsonArray arr = new JsonArray();
                // Use reflection to access the node field
                try {
                    java.lang.reflect.Field field = JsonArray.class.getDeclaredField("node");
                    field.setAccessible(true);
                    Object arrNode = field.get(arr);
                    ((com.fasterxml.jackson.databind.node.ArrayNode) arrNode).addAll((com.fasterxml.jackson.databind.node.ArrayNode) result);
                } catch (Exception e) {
                    arr.add(mapper.convertValue(result, Object.class));
                }
                return arr;
            } else if (result.isTextual()) {
                return result.asText();
            } else if (result.isInt()) {
                return result.asInt();
            } else if (result.isLong()) {
                return result.asLong();
            } else if (result.isDouble()) {
                return result.asDouble();
            } else if (result.isBoolean()) {
                return result.asBoolean();
            } else {
                return result.asText();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
