import cn.qaiu.util.JsVariableExtractor;
import java.util.Map;

public class TestCommentHandling {
    public static void main(String[] args) {
        // 测试各种注释情况
        String jsCode = "// 这是文件开头的注释\n" +
                "var lanosso = ''; // 这是行尾注释\n" +
                "var down_1 = '';\n" +
                "// 这是单独一行的注释\n" +
                "var down_2 = '';\n" +
                "var down_3 = '&toolsdown'; // 行尾注释\n" +
                "var wsk_sign = 'c20230908';\n" +
                "var aihidcms = '';\n" +
                "var ciucjdsdc = '';\n" +
                "var wp_sign = 'UzVWaA08VGUBCFdoBzcAPFQ8BTJeNFBnCj1XYlI_aWmlVYlAhAClXPlI1UDEGZFZkVDwPPFE6BzxXYltv';\n" +
                "var v3v3 = '';\n" +
                "var ajaxdata = '9aPu';\n" +
                "var kdns = 1;\n" +
                "if (typeof(killdns)=='undefined'){\n" +
                "    //var kdns = 0; // 这是注释掉的变量\n" +
                "}\n" +
                "var test1 = 'http://example.com'; // URL中的//不应该被当作注释\n" +
                "var test2 = 'test//test'; // 字符串中的//不应该被当作注释\n" +
                "$.ajax({\n" +
                "    type : 'post',//url : '/ajaxm.php?file=1',//\n" +
                "    url : '/ajaxm.php?file=225503127',//data///////\n" +
                "    data : { 'action':'downprocess','websignkey':ajaxdata,'signs':ajaxdata,'sign':wp_sign,'websign':'','kd':kdns,'ves':1 },//},//'ves':1 },/////\n" +
                "    dataType : 'json',\n" +
                "    success:function(msg){\n" +
                "        var date = msg;\n" +
                "        var dom_down = date.dom;\n" +
                "    }\n" +
                "});";
        
        System.out.println("原始代码:");
        System.out.println(jsCode);
        System.out.println("\n" + "=".repeat(60));
        System.out.println("提取的变量（应该忽略注释中的变量）:");
        System.out.println("=".repeat(60));
        
        Map<String, Object> variables = JsVariableExtractor.extractVariables(jsCode);
        
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            System.out.printf("%-15s = %-50s [%s]\n", 
                entry.getKey(), 
                entry.getValue() != null ? entry.getValue().toString() : "null",
                entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null");
        }
        
        // 验证不应该提取注释中的变量
        if (variables.containsKey("kdns")) {
            Object kdns = variables.get("kdns");
            if (kdns instanceof Integer && ((Integer) kdns) == 1) {
                System.out.println("\n✓ 正确：kdns = 1（注释中的 kdns = 0 被正确忽略）");
            } else {
                System.out.println("\n✗ 错误：kdns 的值不正确");
            }
        }
        
        // 验证字符串中的 // 没有被当作注释
        if (variables.containsKey("test1")) {
            String test1 = (String) variables.get("test1");
            if (test1.contains("//")) {
                System.out.println("✓ 正确：test1 中的 // 没有被当作注释: " + test1);
            }
        }
        
        if (variables.containsKey("test2")) {
            String test2 = (String) variables.get("test2");
            if (test2.contains("//")) {
                System.out.println("✓ 正确：test2 中的 // 没有被当作注释: " + test2);
            }
        }
    }
}

