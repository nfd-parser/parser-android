package io.netty.handler.codec.http;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryStringDecoder {
    private final Map<String, List<String>> parameters = new HashMap<>();

    public QueryStringDecoder(String uri) {
        this(uri, StandardCharsets.UTF_8);
    }

    public QueryStringDecoder(String uri, Charset charset) {
        if (uri != null && uri.contains("?")) {
            String query = uri.substring(uri.indexOf("?") + 1);
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                if (pair.contains("=")) {
                    try {
                        String[] keyValue = pair.split("=", 2);
                        String key = URLDecoder.decode(keyValue[0], charset.name());
                        String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], charset.name()) : "";
                        
                        parameters.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                    } catch (Exception e) {
                        // Ignore decoding errors
                    }
                }
            }
        }
    }

    public Map<String, List<String>> parameters() {
        Map<String, List<String>> result = new HashMap<>();
        parameters.forEach((key, values) -> result.put(key, new ArrayList<>(values)));
        return result;
    }
}
