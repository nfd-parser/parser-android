package cn.qaiu.util;

/**
 * Cookie 工具类，用于处理 UC/夸克等网盘的 Cookie 字符串
 */
public class CookieUtils {

    /**
     * 过滤 UC/夸克 Cookie 字符串，保留有效的认证 Cookie（移除被设置为空的键）
     *
     * @param cookieStr 原始 Cookie 字符串（如 "a=1; b=; c=3"）
     * @return 过滤后的 Cookie 字符串
     */
    public static String filterUcQuarkCookie(String cookieStr) {
        if (cookieStr == null || cookieStr.isEmpty()) {
            return cookieStr;
        }
        StringBuilder sb = new StringBuilder();
        String[] pairs = cookieStr.split(";");
        for (String pair : pairs) {
            String trimmed = pair.trim();
            if (trimmed.isEmpty()) continue;
            int eq = trimmed.indexOf('=');
            if (eq < 0) {
                // 没有等号，保留
                if (sb.length() > 0) sb.append("; ");
                sb.append(trimmed);
                continue;
            }
            String val = trimmed.substring(eq + 1).trim();
            // 过滤掉值为空的条目
            if (!val.isEmpty()) {
                if (sb.length() > 0) sb.append("; ");
                sb.append(trimmed);
            }
        }
        return sb.toString();
    }

    /**
     * 更新 Cookie 字符串中指定键的值
     *
     * @param cookieStr 原始 Cookie 字符串
     * @param key       要更新的 Cookie 键
     * @param newValue  新的值
     * @return 更新后的 Cookie 字符串
     */
    public static String updateCookieValue(String cookieStr, String key, String newValue) {
        if (cookieStr == null || cookieStr.isEmpty()) {
            return key + "=" + newValue;
        }
        StringBuilder sb = new StringBuilder();
        String[] pairs = cookieStr.split(";");
        boolean found = false;
        for (String pair : pairs) {
            String trimmed = pair.trim();
            if (trimmed.isEmpty()) continue;
            if (sb.length() > 0) sb.append("; ");
            int eq = trimmed.indexOf('=');
            if (eq >= 0 && trimmed.substring(0, eq).trim().equals(key)) {
                sb.append(key).append("=").append(newValue);
                found = true;
            } else {
                sb.append(trimmed);
            }
        }
        if (!found) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(key).append("=").append(newValue);
        }
        return sb.toString();
    }
}
