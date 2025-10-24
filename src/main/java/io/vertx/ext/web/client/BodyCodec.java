package io.vertx.ext.web.client;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * 响应体解码器接口
 * 用于将HTTP响应体转换为指定类型
 * 
 * @param <T> 目标类型
 */
public interface BodyCodec<T> {
    
    /**
     * 解码响应体
     * @param body 原始响应体（String格式）
     * @param headers 响应头
     * @return 解码后的对象
     */
    T decode(String body, MultiMap headers);
    
    /**
     * 返回默认的响应类型
     * @return Class对象
     */
    Class<T> getTargetClass();
    
    // ==================== 静态工厂方法 ====================
    
    /**
     * 创建Buffer类型的BodyCodec
     */
    static BodyCodec<Buffer> buffer() {
        return new BodyCodecImpl<>(body -> Buffer.buffer(body != null ? body.getBytes() : new byte[0]), Buffer.class);
    }
    
    /**
     * 创建String类型的BodyCodec
     */
    static BodyCodec<String> string() {
        return new BodyCodecImpl<>(body -> body != null ? body : "", String.class);
    }
    
    /**
     * 创建JsonObject类型的BodyCodec
     */
    static BodyCodec<JsonObject> jsonObject() {
        return new BodyCodecImpl<>(body -> {
            try {
                return new JsonObject(body != null ? body : "{}");
            } catch (Exception e) {
                return new JsonObject();
            }
        }, JsonObject.class);
    }
    
    /**
     * 创建Void类型的BodyCodec（不返回响应体）
     */
    static BodyCodec<Void> none() {
        return new BodyCodecImpl<>(body -> null, Void.class);
    }
    
    /**
     * 通用BodyCodec实现
     */
    class BodyCodecImpl<T> implements BodyCodec<T> {
        private final Function<String, T> decoder;
        private final Class<T> targetClass;
        
        public BodyCodecImpl(Function<String, T> decoder, Class<T> targetClass) {
            this.decoder = decoder;
            this.targetClass = targetClass;
        }
        
        @Override
        public T decode(String body, MultiMap headers) {
            return decoder.apply(body);
        }
        
        @Override
        public Class<T> getTargetClass() {
            return targetClass;
        }
    }
}

