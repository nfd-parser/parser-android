package cn.qaiu.parser;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import static org.junit.Assert.*;

public class RhinoScriptTest {
    private Context context;
    private Scriptable scope;

    @Before
    public void setup() {
        context = Context.enter();
        context.setOptimizationLevel(-1); // 禁用字节码优化，提高兼容性
        scope = context.initStandardObjects();
    }

    @Test
    public void testBasicJavaScript() {
        Object result = context.evaluateString(scope,
            "var x = 10; var y = 20; x + y;",
            "test.js", 1, null);
        assertEquals(30, Context.toNumber(result), 0.0001);
    }

    @Test
    public void testJsonParsing() {
        String script = "var obj = JSON.parse('{\"name\":\"test\",\"value\":123}'); obj.name + obj.value;";
        Object result = context.evaluateString(scope, script, "test.js", 1, null);
        assertEquals("test123", Context.toString(result));
    }

    @Test
    public void testRegexParsing() {
        String script = 
            "var text = 'test123test456';" +
            "var regex = /test(\\d+)/g;" +
            "var matches = [];" +
            "var match;" +
            "while ((match = regex.exec(text)) !== null) {" +
            "  matches.push(match[1]);" +
            "}" +
            "matches.join(',');";
        
        Object result = context.evaluateString(scope, script, "test.js", 1, null);
        assertEquals("123,456", Context.toString(result));
    }

    @Test
    public void testArrayOperations() {
        String script =
            "var arr = [1, 2, 3, 4, 5];" +
            "arr.map(function(x) { return x * 2; })" +
            "   .filter(function(x) { return x > 5; })" +
            "   .join(',');";
        
        Object result = context.evaluateString(scope, script, "test.js", 1, null);
        assertEquals("6,8,10", Context.toString(result));
    }

    @Test
    public void testErrorHandling() {
        try {
            context.evaluateString(scope,
                "throw new Error('test error');",
                "test.js", 1, null);
            fail("Should throw exception");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("test error"));
        }
    }

    @Test
    public void testJavaInterop() {
        // 定义一个 Java 对象供 JavaScript 使用
        Object javaObj = Context.javaToJS(new JavaTestObject(), scope);
        ScriptableObject.putProperty(scope, "javaObj", javaObj);

        String script =
            "javaObj.setValue('test');" +
            "javaObj.getValue();";
        
        Object result = context.evaluateString(scope, script, "test.js", 1, null);
        assertEquals("test", Context.toString(result));
    }

    // 测试用的 Java 类
    public static class JavaTestObject {
        private String value;

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
