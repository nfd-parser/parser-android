package cn.qaiu.util;

import org.mozilla.javascript.Context;

/**
 * Rhino JS -> Java 转换工具
 */
public final class RhinoUtils {
    private RhinoUtils() {}

    /**
     * 将 Rhino 返回的 JS 值转换为 Java String。
     * - 如果传入已经是 String 则直接返回
     * - 优先使用 Context.jsToJava(..., String.class)
     * - 回退使用 Context.toString(...) 或 String.valueOf(...)
     */
    public static String toJavaString(Object jsValue) {
        if (jsValue == null) return null;
        if (jsValue instanceof String) return (String) jsValue;

        Context cx = Context.getCurrentContext();
        boolean entered = false;
        try {
            if (cx == null) {
                Context.enter();
                entered = true;
            }
            try {
                Object converted = Context.jsToJava(jsValue, String.class);
                if (converted instanceof String) {
                    return (String) converted;
                }
            } catch (Throwable ignore) {
                // jsToJava 可能失败，继续回退
            }
            try {
                return Context.toString(jsValue);
            } catch (Throwable ignore) {
                return String.valueOf(jsValue);
            }
        } finally {
            if (entered) {
                Context.exit();
            }
        }
    }
}
