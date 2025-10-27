package cn.qaiu.util;

public class StringUtils {

    /**
     * 判断字符串是否为空或null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否非空且非null
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为空白（null、空字符串或只包含空白字符）
     */
    public static boolean isBlank(String str) {
        if (isEmpty(str)) {
            return true;
        }
        int length = str.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否非空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 右补齐字符串到指定长度，使用指定字符填充
     *
     * @param str 要补齐的字符串
     * @param size 目标长度
     * @param padChar 填充字符
     * @return 补齐后的字符串
     */
    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int strLen = str.length();
        if (strLen >= size) {
            return str;
        }
        StringBuilder sb = new StringBuilder(size);
        sb.append(str);
        for (int i = 0; i < size - strLen; i++) {
            sb.append(padChar);
        }
        return sb.toString();
    }

    // 非贪婪截断匹配
    public static String StringCutNot(final String strtarget, final String strstart)
    {
        int startIdx = strtarget.indexOf(strstart);

        if (startIdx != -1) {
            startIdx += strstart.length();
            return strtarget.substring(startIdx);
        }

        return null;
    }

    // 非贪婪截断匹配
    public static String StringCutNot(final String strtarget, final String strstart, final String strend)
    {
        int startIdx = strtarget.indexOf(strstart);
        int endIdx   = -1;

        if (startIdx != -1) {
            startIdx += strstart.length();
            endIdx    = strtarget.indexOf(strend, startIdx);

            if (endIdx != -1) {
                return strtarget.substring(startIdx, endIdx);
            }
        }

        return null;
    }

}
