package cn.qaiu.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    public static String formatSecondsStringToDateTime(String s) {
        if (s == null || s.isEmpty()) return "";
        try { long v = Long.parseLong(s.trim()); return LocalDateTime.ofInstant(Instant.ofEpochSecond(v), ZONE).format(FORMATTER); } catch (Exception e) { return s; }
    }

    public static String formatMillisStringToDateTime(String s) {
        if (s == null || s.isEmpty()) return "";
        try { long v = Long.parseLong(s.trim()); return LocalDateTime.ofInstant(Instant.ofEpochMilli(v), ZONE).format(FORMATTER); } catch (Exception e) { return s; }
    }

    public static String formatTimestampToDateTime(String s) {
        if (s == null || s.isEmpty()) return "";
        try {
            if (s.contains("T") || (s.contains("-") && s.length() > 8)) return LocalDateTime.ofInstant(Instant.parse(s), ZONE).format(FORMATTER);
            long n = Long.parseLong(s.trim());
            return LocalDateTime.ofInstant(n > 9999999999L ? Instant.ofEpochMilli(n) : Instant.ofEpochSecond(n), ZONE).format(FORMATTER);
        } catch (Exception e) { return s; }
    }
}
