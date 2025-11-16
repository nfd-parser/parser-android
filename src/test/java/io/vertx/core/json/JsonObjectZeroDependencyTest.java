package io.vertx.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

public class JsonObjectZeroDependencyTest {

    @Test
    public void testCreateEmpty() {
        JsonObject obj = new JsonObject();
        assertTrue(obj.isEmpty());
        assertEquals("{}", obj.encode());
    }

    @Test
    public void testPutAndGetString() {
        JsonObject obj = new JsonObject();
        obj.put("key", "value");
        assertEquals("value", obj.getString("key"));
        assertFalse(obj.isEmpty());
    }

    @Test
    public void testPutAndGetInteger() {
        JsonObject obj = new JsonObject();
        obj.put("number", 42);
        assertEquals(Integer.valueOf(42), obj.getInteger("number"));
    }

    @Test
    public void testPutAndGetLong() {
        JsonObject obj = new JsonObject();
        obj.put("long", 1000000000000L);
        assertEquals(Long.valueOf(1000000000000L), obj.getLong("long"));
    }

    @Test
    public void testPutAndGetDouble() {
        JsonObject obj = new JsonObject();
        obj.put("double", 3.14);
        assertEquals(Double.valueOf(3.14), obj.getDouble("double"));
    }

    @Test
    public void testPutAndGetBoolean() {
        JsonObject obj = new JsonObject();
        obj.put("flag", true);
        assertEquals(Boolean.TRUE, obj.getBoolean("flag"));
    }

    @Test
    public void testParseJsonString() {
        String json = "{\"name\":\"test\",\"age\":30}";
        JsonObject obj = new JsonObject(json);
        assertEquals("test", obj.getString("name"));
        assertEquals(Integer.valueOf(30), obj.getInteger("age"));
    }

    @Test
    public void testEncodeJson() {
        JsonObject obj = new JsonObject();
        obj.put("name", "test");
        obj.put("age", 30);
        String encoded = obj.encode();
        assertTrue(encoded.contains("\"name\""));
        assertTrue(encoded.contains("\"test\""));
        assertTrue(encoded.contains("\"age\""));
        assertTrue(encoded.contains("30"));
    }

    @Test
    public void testNestedJsonObject() {
        JsonObject nested = new JsonObject();
        nested.put("nestedKey", "nestedValue");
        
        JsonObject obj = new JsonObject();
        obj.put("nested", nested);
        
        JsonObject retrieved = obj.getJsonObject("nested");
        assertNotNull(retrieved);
        assertEquals("nestedValue", retrieved.getString("nestedKey"));
    }

    @Test
    public void testGetJsonArray() {
        JsonArray arr = new JsonArray();
        arr.add("item1");
        arr.add("item2");
        
        JsonObject obj = new JsonObject();
        obj.put("items", arr);
        
        JsonArray retrieved = obj.getJsonArray("items");
        assertNotNull(retrieved);
        assertEquals(2, retrieved.size());
        assertEquals("item1", retrieved.getString(0));
    }

    @Test
    public void testGetStringWithDefault() {
        JsonObject obj = new JsonObject();
        String value = obj.getString("nonexistent", "default");
        assertEquals("default", value);
    }

    @Test
    public void testContainsKey() {
        JsonObject obj = new JsonObject();
        obj.put("key", "value");
        assertTrue(obj.containsKey("key"));
        assertFalse(obj.containsKey("nonexistent"));
    }

    @Test
    public void testFromMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 42);
        
        JsonObject obj = new JsonObject(map);
        assertEquals("value1", obj.getString("key1"));
        assertEquals(Integer.valueOf(42), obj.getInteger("key2"));
    }

    @Test
    public void testEncodePrettily() {
        JsonObject obj = new JsonObject();
        obj.put("name", "test");
        String pretty = obj.encodePrettily();
        assertNotNull(pretty);
        assertTrue(pretty.contains("test"));
    }

    @Test
    public void testNullHandling() {
        JsonObject obj = new JsonObject();
        obj.put("nullKey", null);
        assertNull(obj.getString("nullKey"));
    }

    @Test
    public void testSpecialStringValues() {
        JsonObject obj = new JsonObject();
        obj.put("newline", "test\nvalue");
        obj.put("tab", "test\tvalue");
        obj.put("quote", "test\"value");
        
        String encoded = obj.encode();
        assertNotNull(encoded);
        
        // Verify it can be parsed back
        JsonObject parsed = new JsonObject(encoded);
        assertTrue(parsed.containsKey("newline"));
        assertTrue(parsed.containsKey("tab"));
        assertTrue(parsed.containsKey("quote"));
    }

    @Test
    public void testDeepNestedJsonWithArray() {
        // 测试用户报告的场景：嵌套较深的 JSON，数组中的对象
        String json = "{\"code\":0,\"message\":\"ok\",\"data\":{\"Next\":\"-1\",\"Len\":1,\"IsFirst\":true,\"Expired\":false,\"InfoList\":[{\"FileId\":8559456,\"FileName\":\"test.apk\",\"Type\":0,\"Size\":161351683}],\"IsPaidPreview\":false}}";
        JsonObject obj = new JsonObject(json);
        
        // 验证顶层字段
        assertEquals(Integer.valueOf(0), obj.getInteger("code"));
        assertEquals("ok", obj.getString("message"));
        
        // 验证嵌套的 data 对象
        JsonObject data = obj.getJsonObject("data");
        assertNotNull(data);
        assertEquals("-1", data.getString("Next"));
        assertEquals(Integer.valueOf(1), data.getInteger("Len"));
        assertTrue(data.getBoolean("IsFirst"));
        assertFalse(data.getBoolean("Expired"));
        assertFalse(data.getBoolean("IsPaidPreview"));
        
        // 验证 InfoList 数组不为空
        JsonArray infoList = data.getJsonArray("InfoList");
        assertNotNull(infoList);
        assertEquals(1, infoList.size());
        
        // 验证数组中的对象
        JsonObject fileInfo = infoList.getJsonObject(0);
        assertNotNull(fileInfo);
        assertEquals(Integer.valueOf(8559456), fileInfo.getInteger("FileId"));
        assertEquals("test.apk", fileInfo.getString("FileName"));
        assertEquals(Integer.valueOf(0), fileInfo.getInteger("Type"));
        assertEquals(Long.valueOf(161351683), fileInfo.getLong("Size"));
    }
}

