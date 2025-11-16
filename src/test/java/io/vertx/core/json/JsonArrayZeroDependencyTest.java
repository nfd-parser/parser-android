package io.vertx.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

public class JsonArrayZeroDependencyTest {

    @Test
    public void testCreateEmpty() {
        JsonArray arr = new JsonArray();
        assertTrue(arr.isEmpty());
        assertEquals(0, arr.size());
        assertEquals("[]", arr.encode());
    }

    @Test
    public void testAddAndGetString() {
        JsonArray arr = new JsonArray();
        arr.add("value");
        assertEquals(1, arr.size());
        assertEquals("value", arr.getString(0));
    }

    @Test
    public void testAddMultipleTypes() {
        JsonArray arr = new JsonArray();
        arr.add("string");
        arr.add(42);
        arr.add(true);
        arr.add(3.14);
        
        assertEquals(4, arr.size());
        assertEquals("string", arr.getString(0));
        assertEquals(Integer.valueOf(42), arr.getInteger(1));
        assertEquals(Boolean.TRUE, arr.getBoolean(2));
        assertEquals(Double.valueOf(3.14), arr.getDouble(3));
    }

    @Test
    public void testParseJsonArrayString() {
        String json = "[\"item1\",\"item2\",\"item3\"]";
        JsonArray arr = new JsonArray(json);
        assertEquals(3, arr.size());
        assertEquals("item1", arr.getString(0));
        assertEquals("item2", arr.getString(1));
        assertEquals("item3", arr.getString(2));
    }

    @Test
    public void testNestedJsonObject() {
        JsonObject nested = new JsonObject();
        nested.put("key", "value");
        
        JsonArray arr = new JsonArray();
        arr.add(nested);
        
        JsonObject retrieved = arr.getJsonObject(0);
        assertNotNull(retrieved);
        assertEquals("value", retrieved.getString("key"));
    }

    @Test
    public void testNestedJsonArray() {
        JsonArray inner = new JsonArray();
        inner.add("inner");
        
        JsonArray outer = new JsonArray();
        outer.add(inner);
        
        JsonArray retrieved = outer.getJsonArray(0);
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());
        assertEquals("inner", retrieved.getString(0));
    }

    @Test
    public void testGetList() {
        JsonArray arr = new JsonArray();
        arr.add("item1");
        arr.add(42);
        
        java.util.List<Object> list = arr.getList();
        assertEquals(2, list.size());
        assertEquals("item1", list.get(0));
        assertEquals(42, list.get(1));
    }

    @Test
    public void testForEach() {
        JsonArray arr = new JsonArray();
        arr.add("item1");
        arr.add("item2");
        
        final int[] count = {0};
        arr.forEach(item -> count[0]++);
        assertEquals(2, count[0]);
    }

    @Test
    public void testFromList() {
        java.util.List<Object> list = new java.util.ArrayList<>();
        list.add("item1");
        list.add("item2");
        
        JsonArray arr = new JsonArray(list);
        assertEquals(2, arr.size());
        assertEquals("item1", arr.getString(0));
        assertEquals("item2", arr.getString(1));
    }

    @Test
    public void testOutOfBounds() {
        JsonArray arr = new JsonArray();
        arr.add("item");
        
        assertNull(arr.getString(1));
        assertNull(arr.getInteger(1));
        assertNull(arr.getBoolean(1));
        assertNull(arr.getDouble(1));
        assertNull(arr.getLong(1));
    }

    @Test
    public void testNullValues() {
        JsonArray arr = new JsonArray();
        arr.add(null);
        arr.add("value");
        
        assertNull(arr.getString(0));
        assertEquals("value", arr.getString(1));
    }

    @Test
    public void testEncodePrettily() {
        JsonArray arr = new JsonArray();
        arr.add("item1");
        arr.add("item2");
        
        String pretty = arr.encodePrettily();
        assertNotNull(pretty);
        assertTrue(pretty.contains("item1"));
        assertTrue(pretty.contains("item2"));
    }

    @Test
    public void testStaticOf() {
        JsonArray arr = JsonArray.of("value");
        assertEquals(1, arr.size());
        assertEquals("value", arr.getString(0));
        
        arr = JsonArray.of("value1", "value2");
        assertEquals(2, arr.size());
        
        arr = JsonArray.of("v1", "v2", "v3");
        assertEquals(3, arr.size());
        
        arr = JsonArray.of("v1", "v2", "v3", "v4");
        assertEquals(4, arr.size());
    }

    @Test
    public void testParseComplexArray() {
        String json = "[{\"name\":\"test\",\"age\":30},{\"name\":\"test2\",\"age\":40}]";
        JsonArray arr = new JsonArray(json);
        assertEquals(2, arr.size());
        
        JsonObject obj1 = arr.getJsonObject(0);
        assertNotNull(obj1);
        assertEquals("test", obj1.getString("name"));
        assertEquals(Integer.valueOf(30), obj1.getInteger("age"));
    }
}

