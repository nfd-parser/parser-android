package io.vertx.core.json.pointer;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonPointerImpl {
    private final JsonPointer pointer;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonPointerImpl(String pointer) {
        this.pointer = JsonPointer.compile(pointer);
    }

    public static JsonPointerImpl of(String pointer) {
        return new JsonPointerImpl(pointer);
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
}
