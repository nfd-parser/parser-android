package cn.qaiu.util;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Random;

/**
 * 签名工具，对应 JS 代码里的签名实现
 * <br>Create date 2024-06-13 11:23:00
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 */
public class SignUtil {

    // ====== 时间结构 ======

    /**
     * 对应 JS 里的时间部分结构体
     */
    public static class TimeParts {
        public int y;
        public String m;
        public String d;
        public String h;
        public String f; // minute
    }

    /**
     * 对应 JS 的 _0x1b5d95
     * @param timestampSeconds 10 位时间戳（秒）
     * @param offsetHours      时区偏移，默认 8（JS 里面相当于 +8 小时）
     */
    public static TimeParts getTimeParts(long timestampSeconds, int offsetHours) {
        // JS 中：
        // _0xc5c54a = tsMillis + 60000 * new Date(tsMillis).getTimezoneOffset()
        // _0x3732dc = _0xc5c54a + 3600000 * offsetHours
        //
        // 在浏览器里 new Date().getTimezoneOffset() 是本地时区相对 UTC 的偏移（分钟）
        // 通常中国区浏览器 offset = -480，这样整体相当于 +8 小时。
        // 这里取简化方案：就直接用 UTC 时间，加 offsetHours 小时。

        long tsMillis = timestampSeconds * 1000L;

        // 以 UTC 为基准，再加 offsetHours
        Instant base = Instant.ofEpochMilli(tsMillis);
        ZonedDateTime zdt = base.atZone(ZoneOffset.UTC).plusHours(-offsetHours);

        TimeParts tp = new TimeParts();
        tp.y = zdt.getYear();
        tp.m = pad2(zdt.getMonthValue());
        tp.d = pad2(zdt.getDayOfMonth());
        tp.h = pad2(zdt.getHour());
        tp.f = pad2(zdt.getMinute());
        return tp;
    }

    public static TimeParts getTimeParts(long timestampSeconds) {
        return getTimeParts(timestampSeconds, 8);
    }

    private static String pad2(int x) {
        return x < 10 ? "0" + x : Integer.toString(x);
    }

    // ====== CRC32 实现，对应 _0x4f141a ======

    private static final int[] CRC_TABLE = new int[256];
    private static boolean CRC_INITED = false;

    private static void initCrcTable() {
        if (CRC_INITED) {
            return;
        }
        for (int i = 0; i < 256; i++) {
            int c = i;
            for (int j = 0; j < 8; j++) {
                if ((c & 1) != 0) {
                    c = 0xEDB88320 ^ (c >>> 1);
                } else {
                    c = c >>> 1;
                }
            }
            CRC_TABLE[i] = c;
        }
        CRC_INITED = true;
    }

    /**
     * 计算字符串的 CRC32，并以指定进制输出
     * 对应 JS 的 _0x4f141a(str, radix)
     */
    public static String crc32String(String input, int radix) {
        initCrcTable();
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        int crc = -1;  // 0xFFFFFFFF

        for (byte b : bytes) {
            int ch = b & 0xFF;
            crc = (crc >>> 8) ^ CRC_TABLE[(crc ^ ch) & 0xFF];
        }

        crc = (~crc); // 等价于 0xFFFFFFFF ^ crc

        // 无符号转成指定进制
        long unsigned = crc & 0xFFFFFFFFL;
        return toUnsignedString(unsigned, radix);
    }

    // 把无符号 long 按 base(2~36) 转成字符串（类似 JS 的 toString(radix)）
    private static String toUnsignedString(long value, int base) {
        if (base < 2 || base > 36) {
            base = 10;
        }
        if (value == 0) {
            return "0";
        }

        char[] digits = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        long v = value;
        while (v != 0) {
            int d = (int) (v % base);
            sb.append(digits[d]);
            v /= base;
        }
        return sb.reverse().toString();
    }

    // ====== getSign，对应 JS 的 getSign(_0x1e37d5) ======

    private static final String KEY_STR = "a,d,e,f,g,h,l,m,y,i,j,n,o,p,k,q,r,s,t,u,b,c,v,w,s,z";
    private static final String[] KEY_ARRAY = KEY_STR.split(",");

    private static final Random RANDOM = new Random();

    /**
     * 对应 JS 里的 getSign(path)
     *
     * @param path 例如 "/b/api/share/download/info"
     * @return 一个数组： [秘钥s1, "timestamp-random-s2"]
     */
    public static String[] getSign(String path) {
        String platform = "web";
        int constantVal = 3;

        // JS 中的时间计算（简化版）：
        long nowMillis = System.currentTimeMillis();
        long tsSec = nowMillis / 1000L;
        String tsStr = Long.toString(tsSec);

        // 随机码 [0, 10^7)
        int randomVal = RANDOM.nextInt(10_000_000);
        String randomStr = Integer.toString(randomVal);

        // 时间字段（使用 tsSec）
        TimeParts t = getTimeParts(tsSec);
        String chars = getChars(t);

        // 第一个 CRC32：s1
        String s1 = crc32String(chars, 10);

        // 第二个 CRC32：对 "ts|random|path|web|3|s1"
        String msg = tsStr + '|' +
                randomStr + '|' +
                path + '|' +
                platform + '|' +
                constantVal + '|' +
                s1;

        String s2 = crc32String(msg, 10);

        String header = tsStr + "-" + randomStr + "-" + s2;

        return new String[]{s1, header};
    }

    @NotNull
    private static String getChars(TimeParts t) {
        String ymddhf = t.y + t.m + t.d + t.h + t.f;

        // 用每一位数字在 KEY_ARRAY 里取字符
        StringBuilder charsBuilder = new StringBuilder();
        for (int i = 0; i < ymddhf.length(); i++) {
            int idx = ymddhf.charAt(i) - '0';
            if (idx >= 0 && idx < KEY_ARRAY.length) {
                charsBuilder.append(KEY_ARRAY[idx]);
            } else {
                // 理论上不会出现
                charsBuilder.append('_');
            }
        }
        return charsBuilder.toString();
    }
}