package cn.qaiu.util;
import org.mozilla.javascript.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 执行Js脚本 - 使用Rhino引擎
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 * Create at 2023/7/29 17:35
 */
public class JsExecUtils {
    private static final ContextFactory contextFactory = new ContextFactory();

    // 初始化脚本引擎
    static {
        // Rhino不需要特殊初始化
    }

    /**
     * 调用js文件
     */
    public static Map<String, Object> executeJs(String functionName, Object... args) {
        Context context = contextFactory.enterContext();
        try {
            Scriptable scope = context.initStandardObjects();
            context.evaluateString(scope, JsContent.ye123, "ye123", 1, null);
            
            // 获取函数对象
            Object func = scope.get(functionName, scope);
            if (!(func instanceof Function)) {
                throw new RuntimeException("函数不存在或不是函数: " + functionName);
            }
            
            // 转换参数为JavaScript值
            Object[] jsArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                jsArgs[i] = Context.javaToJS(args[i], scope);
            }
            
            // 调用函数
            Object result = ((Function) func).call(context, scope, scope, jsArgs);
            
            return jsObjectToMap(result);
        } catch (Exception e) {
            throw new RuntimeException("JS执行失败: " + e.getMessage(), e);
        } finally {
            Context.exit();
        }
    }

    /**
     * 调用执行蓝奏云js文件
     */
    public static Map<String, Object> executeDynamicJs(String jsText, String funName) {
        Context context = contextFactory.enterContext();
        try {
            Scriptable scope = context.initStandardObjects();
            
            // 执行JS代码
            context.evaluateString(scope, JsContent.lz + "\n" + jsText, "lz", 1, null);
            
            // 调用js中的函数（如果有）
            if (StringUtils.isNotEmpty(funName)) {
                context.evaluateString(scope, 
                    "typeof " + funName + " === 'function' ? " + funName + "() : null", 
                    "call", 1, null);
            }

            // 获取signObj
            Object signObj = scope.get("signObj", scope);
            return jsObjectToMap(signObj);
        } catch (Exception e) {
            throw new RuntimeException("JS执行失败: " + e.getMessage(), e);
        } finally {
            Context.exit();
        }
    }

    /**
     * 调用执行其他js文件
     */
    public static Object executeOtherJs(String jsText, String funName, Object... args) {
        Context context = contextFactory.enterContext();
        try {
            Scriptable scope = context.initStandardObjects();
            context.evaluateString(scope, jsText, "script", 1, null);
            
            // 调用js中的函数
            if (StringUtils.isNotEmpty(funName)) {
                Object func = scope.get(funName, scope);
                if (func instanceof Function) {
                    return ((Function) func).call(context, scope, scope, args);
                }
            }
            throw new RuntimeException("funName is null or not a function");
        } catch (Exception e) {
            throw new RuntimeException("JS执行失败: " + e.getMessage(), e);
        } finally {
            Context.exit();
        }
    }
    
    /**
     * 将Rhino的JavaScript对象转换为Map
     */
    private static Map<String, Object> jsObjectToMap(Object obj) {
        if (obj == null || obj instanceof Undefined) {
            return new java.util.HashMap<>();
        }
        
        if (obj instanceof NativeObject) {
            NativeObject nativeObj = (NativeObject) obj;
            Map<String, Object> map = new java.util.HashMap<>();
            for (Object key : nativeObj.getIds()) {
                String keyStr = key.toString();
                Object value = nativeObj.get(keyStr, nativeObj);
                map.put(keyStr, unwrapValue(value));
            }
            return map;
        }
        
        // 如果不是NativeObject，创建一个简单的包装
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("value", unwrapValue(obj));
        return map;
    }
    
    /**
     * 解包Rhino的值
     */
    private static Object unwrapValue(Object value) {
        if (value == null || value instanceof Undefined) {
            return null;
        }
        if (value instanceof NativeObject) {
            return jsObjectToMap(value);
        }
        if (value instanceof NativeArray) {
            return nativeArrayToList((NativeArray) value);
        }
        return Context.jsToJava(value, Object.class);
    }
    
    /**
     * 将NativeArray转换为List
     */
    private static java.util.List<Object> nativeArrayToList(NativeArray array) {
        java.util.List<Object> list = new java.util.ArrayList<>();
        long length = array.getLength();
        for (long i = 0; i < length; i++) {
            list.add(unwrapValue(array.get((int) i, array)));
        }
        return list;
    }

    public static String getKwSign(String s, String pwd) {
        return executeOtherJs(JsContent.kwSignString, "encrypt", s, pwd).toString();
    }

    public static String mgEncData(String data, String key) {
        return executeOtherJs(JsContent.mgJS, "enc", data, key).toString();
    }

    public static void main(String[] args) {
        System.out.println(URLEncoder
                .encode(mgEncData("{\"copyrightId\":\"6326951FKBL\",\"type\":1,\"auditionsFlag\":0}", AESUtils.MG_KEY), StandardCharsets.UTF_8));
    }
}
