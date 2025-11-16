package io.vertx.core.json.pointer;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

public class JsonPointer {
    private final JsonPointerImpl impl;

    public JsonPointer(String pointer) {
        this.impl = new JsonPointerImpl(pointer);
    }

    public static JsonPointer of(String pointer) {
        return new JsonPointer(pointer);
    }

    public static JsonPointer from(String pointer) {
        return new JsonPointer(pointer);
    }

    public String queryFrom(JsonObject jsonObject) {
        return impl.queryFrom(jsonObject);
    }

    public String queryFrom(JsonArray jsonArray) {
        return impl.queryFrom(jsonArray);
    }

    // These methods are not supported anymore without Jackson - return null
    public Object queryNodeFrom(JsonObject jsonObject) {
        return null;
    }

    public Object queryNodeFrom(JsonArray jsonArray) {
        return null;
    }

    public Object queryJson(JsonObject jsonObject) {
        return impl.queryValueFrom(jsonObject);
    }
}
