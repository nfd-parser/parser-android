package cn.qaiu.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;

public class JsVariableExtractorTest {

    @Test
    public void testExtractVariables() {
        String jsCode = "var lanosso = '';\n" +
                "var down_1 = '';\n" +
                "var down_2 = '';\n" +
                "var down_3 = '&toolsdown';\n" +
                "var wsk_sign = 'c20230908';\n" +
                "var aihidcms = '';\n" +
                "var ciucjdsdc = '';\n" +
                "var wp_sign = 'UzVWaA08VGUBCFdoBzcAPFQ8BTJeNFBnCj1XYlI_aWmlVYlAhAClXPlI1UDEGZFZkVDwPPFE6BzxXYltv';\n" +
                "var v3v3 = '';\n" +
                "var ajaxdata = '9aPu';\n" +
                "var kdns = 1;\n" +
                "if (typeof(killdns)=='undefined'){\n" +
                "    //var kdns = 0;\n" +
                "}";
        
        Map<String, Object> variables = JsVariableExtractor.extractVariables(jsCode);
        
        // 验证变量数量
        assertTrue("应该提取到至少10个变量", variables.size() >= 10);
        
        // 验证字符串变量
        assertEquals("", variables.get("lanosso"));
        assertEquals("", variables.get("down_1"));
        assertEquals("&toolsdown", variables.get("down_3"));
        assertEquals("c20230908", variables.get("wsk_sign"));
        assertEquals("UzVWaA08VGUBCFdoBzcAPFQ8BTJeNFBnCj1XYlI_aWmlVYlAhAClXPlI1UDEGZFZkVDwPPFE6BzxXYltv", 
                variables.get("wp_sign"));
        assertEquals("9aPu", variables.get("ajaxdata"));
        
        // 验证数字变量
        Object kdns = variables.get("kdns");
        assertNotNull("kdns 应该存在", kdns);
        assertTrue("kdns 应该是 Integer 类型", kdns instanceof Integer);
        assertEquals(Integer.valueOf(1), kdns);
    }
    
    @Test
    public void testExtractWithDifferentTypes() {
        String jsCode = "var str1 = 'hello';\n" +
                "var str2 = \"world\";\n" +
                "var num1 = 123;\n" +
                "var num2 = -456;\n" +
                "var num3 = 3.14;\n" +
                "var bool1 = true;\n" +
                "var bool2 = false;\n" +
                "var nullVal = null;\n" +
                "var undefinedVal = undefined;";
        
        Map<String, Object> variables = JsVariableExtractor.extractVariables(jsCode);
        
        assertEquals("hello", variables.get("str1"));
        assertEquals("world", variables.get("str2"));
        assertEquals(Integer.valueOf(123), variables.get("num1"));
        assertEquals(Integer.valueOf(-456), variables.get("num2"));
        assertEquals(Double.valueOf(3.14), variables.get("num3"));
        assertEquals(Boolean.TRUE, variables.get("bool1"));
        assertEquals(Boolean.FALSE, variables.get("bool2"));
        assertNull(variables.get("nullVal"));
        assertNull(variables.get("undefinedVal"));
    }
    
    @Test
    public void testExtractWithComments() {
        String jsCode = "// 这是注释\n" +
                "var var1 = 'value1'; // 行尾注释\n" +
                "/* 多行注释\n" +
                "   第二行 */\n" +
                "var var2 = 'value2';";
        
        Map<String, Object> variables = JsVariableExtractor.extractVariables(jsCode);
        
        assertEquals("value1", variables.get("var1"));
        assertEquals("value2", variables.get("var2"));
    }
    
    @Test
    public void testExtractWithEscapedStrings() {
        String jsCode = "var str1 = 'hello\\nworld';\n" +
                "var str2 = \"test\\tvalue\";\n" +
                "var str3 = 'quote\\'test';\n" +
                "var str4 = \"quote\\\"test\";";
        
        Map<String, Object> variables = JsVariableExtractor.extractVariables(jsCode);
        
        assertEquals("hello\nworld", variables.get("str1"));
        assertEquals("test\tvalue", variables.get("str2"));
        assertEquals("quote'test", variables.get("str3"));
        assertEquals("quote\"test", variables.get("str4"));
    }
    
    @Test
    public void testFormatVariables() {
        String jsCode = "var name = 'test';\nvar count = 42;";
        Map<String, Object> variables = JsVariableExtractor.extractVariables(jsCode);
        String formatted = JsVariableExtractor.formatVariables(variables);
        
        assertNotNull(formatted);
        assertTrue(formatted.contains("name"));
        assertTrue(formatted.contains("test"));
        assertTrue(formatted.contains("count"));
        assertTrue(formatted.contains("42"));
    }
}

