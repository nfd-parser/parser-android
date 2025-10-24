package io.vertx.ext.web.client;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

public class HttpResponse<T> {
    
    // Runtime cast helper - safe due to type erasure
    @SuppressWarnings("unchecked")
    public static <U> HttpResponse<U> cast(Object response) {
        return (HttpResponse<U>) response;
    }
    private final int statusCode;
    private final T body;
    private final MultiMap headers;
    private final List<String> cookies = new ArrayList<>();

    public HttpResponse(int code, T body, MultiMap headers) {
        this.statusCode = code;
        this.body = body;
        this.headers = headers;
        
        // Extract cookies from Set-Cookie headers
        if (headers != null) {
            List<String> setCookieHeaders = headers.getAll("Set-Cookie");
            if (setCookieHeaders != null) {
                cookies.addAll(setCookieHeaders);
            }
        }
    }

    public int statusCode() {
        return statusCode;
    }

    public String bodyAsString() {
        if (body instanceof String) {
            return (String) body;
        }
        return body != null ? body.toString() : "";
    }

    public Buffer bodyAsBuffer() {
        if (body instanceof Buffer) {
            return (Buffer) body;
        }
        if (body instanceof String) {
            return Buffer.buffer(((String) body).getBytes(StandardCharsets.UTF_8));
        }
        return Buffer.buffer(body != null ? body.toString().getBytes(StandardCharsets.UTF_8) : new byte[0]);
    }

    public JsonObject bodyAsJsonObject() {
        if (body instanceof JsonObject) {
            return (JsonObject) body;
        }
        try {
            return new JsonObject(body != null ? body.toString() : "{}");
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public MultiMap headers() {
        return headers;
    }

    public List<String> cookies() {
        return new ArrayList<>(cookies);
    }

    // Generic body method - returns the typed body
    public T body() {
        return body;
    }
    
    // Alias for compatibility
    public T bodyAsGeneric() {
        return body;
    }

    // Type conversion support - for compatibility (uses type erasure)
    @SuppressWarnings("unchecked")
    public <U> HttpResponse<U> as() {
        return (HttpResponse<U>) this;
    }
    
    // Return raw type for compatibility
    @SuppressWarnings("rawtypes")
    public HttpResponse asRaw() {
        return this;
    }

    public String toString() {
        return "HttpResponse{statusCode=" + statusCode + ", body='" + body + "'}";
    }
}