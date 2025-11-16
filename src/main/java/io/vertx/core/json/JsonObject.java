package io.vertx.core.json;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonObject {
    public final Map<String, Object> map;

    public JsonObject() {
        this.map = new LinkedHashMap<>();
    }

    public JsonObject(String json) {
        this.map = new LinkedHashMap<>();
        if (json == null || json.trim().isEmpty() || json.trim().equals("{}")) {
            return;
        }
        try {
            parseJson(json, map);
        } catch (Exception e) {
            // Keep empty map on parse error
        }
    }

    public JsonObject(Map<String, Object> map) {
        this.map = new LinkedHashMap<>();
        if (map != null) {
            this.map.putAll(map);
        }
    }

    public JsonObject put(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public String getString(String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return String.valueOf(value);
    }

    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return value != null ? value : defaultValue;
    }

    public Integer getInteger(String key) {
        Object value = map.get(key);
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

    public Long getLong(String key) {
        Object value = map.get(key);
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

    public Double getDouble(String key) {
        Object value = map.get(key);
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

    public Boolean getBoolean(String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public JsonObject getJsonObject(String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof JsonObject) {
            return (JsonObject) value;
        }
        if (value instanceof Map) {
            JsonObject result = new JsonObject();
            @SuppressWarnings("unchecked")
            Map<String, Object> valueMap = (Map<String, Object>) value;
            result.map.putAll(valueMap);
            return result;
        }
        return null;
    }

    public JsonArray getJsonArray(String key) {
        Object value = map.get(key);
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
            valueList.forEach(result::add);
            return result;
        }
        return null;
    }

    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    public String encode() {
        return encodeJson(this, 0);
    }

    public String encodePrettily() {
        return encodeJson(this, 2);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap() {
        return new HashMap<>(map);
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

    @Override
    public String toString() {
        return encode();
    }

    // Internal helper methods for JSON parsing and encoding
    
    // Helper class for parsing results
    private static class ValueResult {
        final Object value;
        final int endPos;

        ValueResult(Object value, int endPos) {
            this.value = value;
            this.endPos = endPos;
        }
    }

    static void parseJsonArray(String json, JsonArray targetArray) {
        if (json == null || json.trim().isEmpty()) {
            return;
        }
        json = json.trim();
        if (!json.startsWith("[") || !json.endsWith("]")) {
            return;
        }
        json = json.substring(1, json.length() - 1).trim();
        
        int pos = 0;
        while (pos < json.length()) {
            pos = skipWhitespace(json, pos);
            if (pos >= json.length()) break;
            
            if (json.charAt(pos) == ']') break;
            
            ValueResult valueResult = parseValue(json, pos);
            if (valueResult != null) {
                targetArray.list.add(valueResult.value);
                pos = valueResult.endPos;
                pos = skipWhitespace(json, pos);
                
                if (pos >= json.length()) break;
                if (json.charAt(pos) == ',') {
                    pos++;
                } else if (json.charAt(pos) == ']') {
                    break;
                }
            } else {
                break;
            }
        }
    }

    private static void parseJson(String json, Map<String, Object> targetMap) {
        if (json == null || json.trim().isEmpty()) {
            return;
        }
        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            return;
        }
        json = json.substring(1, json.length() - 1).trim();
        
        int pos = 0;
        while (pos < json.length()) {
            pos = skipWhitespace(json, pos);
            if (pos >= json.length()) break;
            
            // Parse key
            if (json.charAt(pos) != '"') return;
            pos++;
            int keyStart = pos;
            int keyEnd = skipString(json, pos);
            if (keyEnd < 0) return;
            String key = json.substring(keyStart, keyEnd);
            pos = keyEnd + 1;
            
            pos = skipWhitespace(json, pos);
            if (pos >= json.length() || json.charAt(pos) != ':') return;
            pos++;
            
            // Parse value
            pos = skipWhitespace(json, pos);
            ValueResult valueResult = parseValue(json, pos);
            if (valueResult == null) return;
            targetMap.put(key, valueResult.value);
            
            pos = valueResult.endPos;
            pos = skipWhitespace(json, pos);
            
            if (pos >= json.length()) break;
            if (json.charAt(pos) == ',') {
                pos++;
            } else if (json.charAt(pos) == '}') {
                break;
            } else {
                return;
            }
        }
    }

    private static int skipWhitespace(String json, int pos) {
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    private static int skipString(String json, int pos) {
        while (pos < json.length()) {
            char c = json.charAt(pos);
            if (c == '\\') {
                pos += 2;
            } else if (c == '"') {
                return pos;
            } else {
                pos++;
            }
        }
        return -1;
    }

    private static ValueResult parseValue(String json, int pos) {
        pos = skipWhitespace(json, pos);
        if (pos >= json.length()) return null;
        
        char c = json.charAt(pos);
        if (c == '"') {
            // String
            int start = pos + 1;
            int end = skipString(json, start);
            if (end < 0) return null;
            String str = json.substring(start, end);
            return new ValueResult(str, end + 1);
        } else if (c == '{') {
            // Object - 先找到结束位置，然后解析整个对象字符串
            int endPos = findEndBrace(json, pos);
            if (endPos <= pos) return null;
            String objStr = json.substring(pos, endPos);
            JsonObject obj = new JsonObject();
            parseJson(objStr, obj.map);
            return new ValueResult(obj, endPos);
        } else if (c == '[') {
            // Array - 先找到结束位置，然后解析整个数组字符串
            int endPos = findEndBracket(json, pos);
            if (endPos <= pos) return null;
            String arrStr = json.substring(pos, endPos);
            JsonArray arr = new JsonArray();
            parseJsonArray(arrStr, arr);
            return new ValueResult(arr, endPos);
        } else if (c == 'n' && json.startsWith("null", pos)) {
            return new ValueResult(null, pos + 4);
        } else if (c == 't' && json.startsWith("true", pos)) {
            return new ValueResult(Boolean.TRUE, pos + 4);
        } else if (c == 'f' && json.startsWith("false", pos)) {
            return new ValueResult(Boolean.FALSE, pos + 5);
        } else {
            // Number
            int start = pos;
            boolean hasDecimal = false;
            while (pos < json.length()) {
                char ch = json.charAt(pos);
                if (ch == '.' || ch == 'e' || ch == 'E' || ch == '+' || ch == '-') {
                    hasDecimal = true;
                    pos++;
                } else if (Character.isDigit(ch)) {
                    pos++;
                } else {
                    break;
                }
            }
            if (pos > start) {
                String numStr = json.substring(start, pos);
                try {
                    if (hasDecimal) {
                        return new ValueResult(Double.parseDouble(numStr), pos);
                    } else {
                        long longVal = Long.parseLong(numStr);
                        if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                            return new ValueResult((int) longVal, pos);
                        }
                        return new ValueResult(longVal, pos);
                    }
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private static int findEndBrace(String json, int start) {
        int count = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') count++;
            else if (c == '}') {
                count--;
                if (count == 0) return i + 1;
            }
        }
        return json.length();
    }

    private static int findEndBracket(String json, int start) {
        int count = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') count++;
            else if (c == ']') {
                count--;
                if (count == 0) return i + 1;
            }
        }
        return json.length();
    }


    private static String encodeJson(Object obj, int indent) {
        if (obj instanceof JsonObject) {
            return encodeJsonObject((JsonObject) obj, indent, 0);
        } else if (obj instanceof JsonArray) {
            return encodeJsonArray((JsonArray) obj, indent, 0);
        }
        return encodeValue(obj, indent, 0);
    }

    private static String encodeJsonObject(JsonObject obj, int indent, int level) {
        if (obj.map.isEmpty()) {
            return "{}";
        }
        String indentStr = indent > 0 ? "\n" + "  ".repeat(level) : "";
        String innerIndent = indent > 0 ? "\n" + "  ".repeat(level + 1) : "";
        
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : obj.map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(innerIndent);
            sb.append("\"").append(escapeString(entry.getKey())).append("\":");
            if (indent > 0) sb.append(" ");
            sb.append(encodeValue(entry.getValue(), indent, level + 1));
        }
        sb.append(indentStr).append("}");
        return sb.toString();
    }

    static String encodeJsonArray(JsonArray arr, int indent, int level) {
        if (arr.list.isEmpty()) {
            return "[]";
        }
        String indentStr = indent > 0 ? "\n" + "  ".repeat(level) : "";
        String innerIndent = indent > 0 ? "\n" + "  ".repeat(level + 1) : "";
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(innerIndent);
            sb.append(encodeValue(arr.list.get(i), indent, level + 1));
        }
        sb.append(indentStr).append("]");
        return sb.toString();
    }

    private static String encodeValue(Object value, int indent, int level) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escapeString((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof JsonObject) {
            return encodeJsonObject((JsonObject) value, indent, level);
        } else if (value instanceof JsonArray) {
            return encodeJsonArray((JsonArray) value, indent, level);
        } else if (value instanceof Map) {
            JsonObject tempObj = new JsonObject();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            tempObj.map.putAll(map);
            return encodeJsonObject(tempObj, indent, level);
        } else if (value instanceof java.util.List) {
            JsonArray tempArr = new JsonArray();
            @SuppressWarnings("unchecked")
            java.util.List<Object> list = (java.util.List<Object>) value;
            tempArr.list.addAll(list);
            return encodeJsonArray(tempArr, indent, level);
        } else {
            return "\"" + escapeString(value.toString()) + "\"";
        }
    }

    private static String escapeString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

}
