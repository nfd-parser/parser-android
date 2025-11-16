package io.vertx.core.json.pointer;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

public class JsonPointerImpl {
    private final String[] pathSegments;

    public JsonPointerImpl(String pointer) {
        if (pointer == null || !pointer.startsWith("/")) {
            this.pathSegments = new String[0];
        } else {
            // Parse pointer like "/a/b/0" into segments ["a", "b", "0"]
            String path = pointer.substring(1); // Remove leading "/"
            if (path.isEmpty()) {
                this.pathSegments = new String[0];
            } else {
                this.pathSegments = path.split("/");
                // Unescape ~0 and ~1
                for (int i = 0; i < pathSegments.length; i++) {
                    pathSegments[i] = pathSegments[i].replace("~1", "/").replace("~0", "~");
                }
            }
        }
    }

    public static JsonPointerImpl of(String pointer) {
        return new JsonPointerImpl(pointer);
    }

    public String queryFrom(JsonObject jsonObject) {
        Object result = queryValue(jsonObject, 0);
        if (result == null) {
            return null;
        }
        if (result instanceof String) {
            return (String) result;
        }
        return result.toString();
    }

    public String queryFrom(JsonArray jsonArray) {
        Object result = queryValue(jsonArray, 0);
        if (result == null) {
            return null;
        }
        if (result instanceof String) {
            return (String) result;
        }
        return result.toString();
    }
    
    public Object queryValueFrom(JsonObject jsonObject) {
        return queryValue(jsonObject, 0);
    }
    
    public Object queryValueFrom(JsonArray jsonArray) {
        return queryValue(jsonArray, 0);
    }
    
    private Object queryValue(Object current, int segmentIndex) {
        if (current instanceof JsonObject) {
            return queryValue(((JsonObject) current).getMap(), segmentIndex);
        } else if (current instanceof JsonArray) {
            return queryValue(((JsonArray) current).getList(), segmentIndex);
        }
        return query(current, segmentIndex);
    }

    @SuppressWarnings("unchecked")
    private Object query(Object current, int segmentIndex) {
        if (segmentIndex >= pathSegments.length) {
            return current;
        }

        if (current == null) {
            return null;
        }

        String segment = pathSegments[segmentIndex];
        
        if (current instanceof JsonObject) {
            return query(((JsonObject) current).map, segmentIndex);
        } else if (current instanceof java.util.Map) {
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) current;
            Object next = map.get(segment);
            return query(next, segmentIndex + 1);
        } else if (current instanceof JsonArray) {
            return query(((JsonArray) current).list, segmentIndex);
        } else if (current instanceof java.util.List) {
            java.util.List<Object> list = (java.util.List<Object>) current;
            try {
                int index = Integer.parseInt(segment);
                if (index >= 0 && index < list.size()) {
                    Object next = list.get(index);
                    return query(next, segmentIndex + 1);
                }
            } catch (NumberFormatException e) {
                return null;
            }
            return null;
        }
        
        return null;
    }
}
