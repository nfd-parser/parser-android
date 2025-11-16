package cn.qaiu.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JavaScript 全局变量提取工具类
 * 用于从 JavaScript 代码中提取所有全局变量声明
 */
public class JsVariableExtractor {
    
    // 匹配 var/let/const 变量声明，支持单行和多行
    // 匹配模式：var variableName = value;
    // 注意：由于注释已经在 removeComments 中移除，这里只需要匹配到分号或行尾
    private static final Pattern VAR_PATTERN = Pattern.compile(
        "(?:^|[\\s;{}])(?:var|let|const)\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*=\\s*([^;]+?)(?=\\s*;|\\s*$|\\s*\\n)",
        Pattern.MULTILINE | Pattern.DOTALL
    );
    
    // 匹配字符串值（单引号或双引号）
    private static final Pattern STRING_PATTERN = Pattern.compile(
        "^(['\"])(.*?)\\1$"
    );
    
    // 匹配数字值
    private static final Pattern NUMBER_PATTERN = Pattern.compile(
        "^\\s*(-?\\d+(?:\\.\\d+)?)\\s*$"
    );
    
    // 匹配布尔值
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile(
        "^\\s*(true|false)\\s*$",
        Pattern.CASE_INSENSITIVE
    );
    
    // 匹配 null/undefined
    private static final Pattern NULL_PATTERN = Pattern.compile(
        "^\\s*(null|undefined)\\s*$",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * 从 JavaScript 代码中提取所有全局变量
     * 
     * @param jsCode JavaScript 代码字符串
     * @return Map<String, Object> 变量名和值的映射
     */
    public static Map<String, Object> extractVariables(String jsCode) {
        Map<String, Object> variables = new LinkedHashMap<>();
        
        if (jsCode == null || jsCode.trim().isEmpty()) {
            return variables;
        }
        
        // 移除注释（简单处理，不处理字符串中的注释符号）
        String cleanedCode = removeComments(jsCode);
        
        Matcher matcher = VAR_PATTERN.matcher(cleanedCode);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            String varValue = matcher.group(2).trim();
            
            // 跳过空值或只包含空白字符的值
            if (varValue.isEmpty()) {
                variables.put(varName, "");
                continue;
            }
            
            Object parsedValue = parseValue(varValue);
            variables.put(varName, parsedValue);
        }
        
        return variables;
    }
    
    /**
     * 解析变量值，返回对应的 Java 对象类型
     * 
     * @param valueStr 变量值的字符串表示
     * @return 解析后的值对象
     */
    private static Object parseValue(String valueStr) {
        valueStr = valueStr.trim();
        
        // 处理字符串值
        Matcher stringMatcher = STRING_PATTERN.matcher(valueStr);
        if (stringMatcher.matches()) {
            String quote = stringMatcher.group(1);
            String content = stringMatcher.group(2);
            // 处理转义字符
            content = unescapeString(content, quote);
            return content;
        }
        
        // 处理数字值
        Matcher numberMatcher = NUMBER_PATTERN.matcher(valueStr);
        if (numberMatcher.matches()) {
            String numStr = numberMatcher.group(1);
            if (numStr.contains(".")) {
                try {
                    return Double.parseDouble(numStr);
                } catch (NumberFormatException e) {
                    return valueStr;
                }
            } else {
                try {
                    long longVal = Long.parseLong(numStr);
                    if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                        return (int) longVal;
                    }
                    return longVal;
                } catch (NumberFormatException e) {
                    return valueStr;
                }
            }
        }
        
        // 处理布尔值
        Matcher booleanMatcher = BOOLEAN_PATTERN.matcher(valueStr);
        if (booleanMatcher.matches()) {
            return Boolean.parseBoolean(booleanMatcher.group(1).toLowerCase());
        }
        
        // 处理 null/undefined
        Matcher nullMatcher = NULL_PATTERN.matcher(valueStr);
        if (nullMatcher.matches()) {
            return null;
        }
        
        // 其他情况返回原始字符串
        return valueStr;
    }
    
    /**
     * 处理字符串中的转义字符
     * 
     * @param str 原始字符串
     * @param quote 引号类型（' 或 "）
     * @return 处理后的字符串
     */
    private static String unescapeString(String str, String quote) {
        if (str == null) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);
                switch (next) {
                    case 'n':
                        result.append('\n');
                        i++;
                        break;
                    case 'r':
                        result.append('\r');
                        i++;
                        break;
                    case 't':
                        result.append('\t');
                        i++;
                        break;
                    case '\\':
                        result.append('\\');
                        i++;
                        break;
                    case '\'':
                        result.append('\'');
                        i++;
                        break;
                    case '"':
                        result.append('"');
                        i++;
                        break;
                    default:
                        result.append(c);
                        break;
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    /**
     * 移除 JavaScript 注释（简单实现）
     * 正确处理字符串中的单行注释和多行注释，不会将它们当作注释
     * 
     * @param code JavaScript 代码
     * @return 移除注释后的代码
     */
    private static String removeComments(String code) {
        if (code == null) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        char stringChar = 0;
        boolean inSingleLineComment = false;
        boolean inMultiLineComment = false;
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            char next = (i + 1 < code.length()) ? code.charAt(i + 1) : 0;
            char prev = (i > 0) ? code.charAt(i - 1) : 0;
            
            // 如果在字符串内，先检查是否是字符串的结束
            if (inString) {
                // 检查是否是转义的引号
                boolean isEscaped = (i > 0 && prev == '\\');
                // 检查是否是转义的转义符（\\" 或 \\'），这种情况下引号不是转义的
                if (isEscaped && i > 1 && code.charAt(i - 2) == '\\') {
                    isEscaped = false; // \\" 表示转义的反斜杠 + 引号，引号不是转义的
                }
                
                if ((c == '"' || c == '\'') && c == stringChar && !isEscaped) {
                    // 结束字符串
                    inString = false;
                    stringChar = 0;
                    result.append(c);
                    continue;
                }
                
                // 字符串内的所有字符都直接添加（包括 // 和 /* */）
                result.append(c);
                continue;
            }
            
            // 不在字符串内，检查是否是字符串的开始
            if (!inSingleLineComment && !inMultiLineComment) {
                // 检查是否是转义的引号
                boolean isEscaped = (i > 0 && prev == '\\');
                // 检查是否是转义的转义符（\\" 或 \\'），这种情况下引号不是转义的
                if (isEscaped && i > 1 && code.charAt(i - 2) == '\\') {
                    isEscaped = false; // \\" 表示转义的反斜杠 + 引号，引号不是转义的
                }
                
                if ((c == '"' || c == '\'') && !isEscaped) {
                    // 开始字符串
                    inString = true;
                    stringChar = c;
                    result.append(c);
                    continue;
                }
            }
            
            // 处理单行注释 //
            if (c == '/' && next == '/' && !inMultiLineComment) {
                inSingleLineComment = true;
                i++; // 跳过下一个字符 '/'
                continue;
            }
            
            // 在单行注释中，直到遇到换行符
            if (inSingleLineComment) {
                if (c == '\n' || c == '\r') {
                    inSingleLineComment = false;
                    result.append(c);
                }
                // 注释内容不添加到结果中
                continue;
            }
            
            // 处理多行注释 /* */
            if (c == '/' && next == '*' && !inMultiLineComment) {
                inMultiLineComment = true;
                i++; // 跳过下一个字符 '*'
                continue;
            }
            
            // 在多行注释中，直到遇到 */
            if (inMultiLineComment) {
                if (c == '*' && next == '/') {
                    inMultiLineComment = false;
                    i++; // 跳过下一个字符 '/'
                }
                // 注释内容不添加到结果中
                continue;
            }
            
            // 正常字符，添加到结果中
            result.append(c);
        }
        
        return result.toString();
    }
    
    /**
     * 获取变量值的字符串表示（用于调试）
     * 
     * @param variables 变量映射
     * @return 格式化的字符串
     */
    public static String formatVariables(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" = ");
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("'").append(value).append("'");
            } else {
                sb.append(value);
            }
            sb.append(" (").append(value != null ? value.getClass().getSimpleName() : "null").append(")\n");
        }
        sb.append("}");
        return sb.toString();
    }
}

