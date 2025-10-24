package cn.qaiu.util;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.core.json.JsonObject;
//import org.brotli.dec.BrotliInputStream;
import org.brotli.dec.BrotliInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class HttpResponseHelper {
    static Logger LOGGER = LoggerFactory.getLogger(HttpResponseHelper.class);

    // -------------------- 公共方法 --------------------
    public static String asText(HttpResponse<?> res) {
        String encoding = res.getHeader(HttpHeaders.CONTENT_ENCODING);
        try {
            Buffer body = toBuffer(res);
            if (encoding == null || "identity".equalsIgnoreCase(encoding)) {
                // Content-Encoding为null或identity，先尝试直接转换为文本
                String text = body.toString(StandardCharsets.UTF_8);
                
                // 检查是否可能是压缩数据（包含乱码字符）
                if (containsGarbledChars(text)) {
                    // 可能是压缩数据但Content-Encoding头丢失了，尝试解压
                    LOGGER.warn("检测到可能的压缩数据但Content-Encoding为null，尝试解压");
                    return tryDecompress(body);
                }
                return text;
            }
            return decompress(body, encoding);
        } catch (Exception e) {
            LOGGER.error("asText: {}", e.getMessage(), e);
            return null;
        }
    }
    
    // 检查文本是否包含乱码字符
    private static boolean containsGarbledChars(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        // 检查是否包含控制字符或常见乱码字符
        for (char c : text.toCharArray()) {
            if (c < 32 && c != '\n' && c != '\r' && c != '\t') {
                return true;
            }
        }
        return false;
    }
    
    // 尝试解压（gzip、deflate、br）
    private static String tryDecompress(Buffer compressed) {
        // 按顺序尝试gzip、deflate、br
        String[] encodings = {"gzip", "deflate", "br"};
        for (String encoding : encodings) {
            try {
                String result = decompress(compressed, encoding);
                LOGGER.info("成功使用{}解压数据", encoding);
                return result;
            } catch (Exception e) {
                // 继续尝试下一个
            }
        }
        // 所有尝试都失败，返回原始内容
        LOGGER.warn("所有解压尝试都失败，返回原始内容");
        return compressed.toString(StandardCharsets.UTF_8);
    }

    public static JsonObject asJson(HttpResponse<?> res) {
        try {
            String text = asText(res);
            if (text != null) {
                return new JsonObject(text);
            } else  {
                LOGGER.error("asJson: asText响应数据为空");
                return JsonObject.of();
            }
        } catch (Exception e) {
            LOGGER.error("asJson: {}", e.getMessage(), e);
            return JsonObject.of();
        }
    }

    // -------------------- Buffer 转换 --------------------
    private static Buffer toBuffer(HttpResponse<?> res) {
        // 直接使用 bodyAsBuffer() 方法，它会正确处理 Buffer 和 String 类型
        return res.bodyAsBuffer();
    }

    // -------------------- 通用解压分发 --------------------
    private static String decompress(Buffer compressed, String encoding) throws IOException {
        return switch (encoding.toLowerCase()) {
            case "gzip" -> decompressGzip(compressed);
            case "deflate" -> decompressDeflate(compressed);
            case "br" -> decompressBrotli(compressed);
            case "zstd" -> compressed.toString(StandardCharsets.UTF_8); // 暂时返回原始内容
            default -> throw new UnsupportedOperationException("不支持的 Content-Encoding: " + encoding);
        };
    }

    // -------------------- gzip --------------------
    private static String decompressGzip(Buffer compressed) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressed.getBytes());
             GZIPInputStream gzis = new GZIPInputStream(bais);
             InputStreamReader isr = new InputStreamReader(gzis, StandardCharsets.UTF_8);
             StringWriter writer = new StringWriter()) {

            char[] buffer = new char[4096];
            int n;
            while ((n = isr.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            return writer.toString();
        }
    }

    // -------------------- deflate --------------------
    private static String decompressDeflate(Buffer compressed) throws IOException {
        byte[] bytes = compressed.getBytes();
        try {
            return inflate(bytes, false); // zlib 包裹
        } catch (IOException e) {
            return inflate(bytes, true); // 裸 deflate
        }
    }

    private static String inflate(byte[] data, boolean nowrap) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             InflaterInputStream iis = new InflaterInputStream(bais, new Inflater(nowrap));
             InputStreamReader isr = new InputStreamReader(iis, StandardCharsets.UTF_8);
             StringWriter writer = new StringWriter()) {

            char[] buffer = new char[4096];
            int n;
            while ((n = isr.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            return writer.toString();
        }
    }

    // -------------------- Brotli --------------------
    private static String decompressBrotli(Buffer compressed) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressed.getBytes());
             BrotliInputStream bis = new BrotliInputStream(bais);
             InputStreamReader isr = new InputStreamReader(bis, StandardCharsets.UTF_8);
             StringWriter writer = new StringWriter()) {

            char[] buffer = new char[4096];
            int n;
            while ((n = isr.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            return writer.toString();
        }
    }

    // -------------------- Zstandard --------------------
    private static String decompressZstd(Buffer compressed) {
       throw new UnsupportedOperationException("Zstandard");
    }
}
