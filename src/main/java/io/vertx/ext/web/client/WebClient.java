package io.vertx.ext.web.client;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import okhttp3.*;

import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.Map;

public class WebClient {
    private final OkHttpClient client;

    public WebClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(false)
                .callTimeout(15, TimeUnit.SECONDS);
        
        // 禁用OkHttp的自动gzip处理，让客户端完全控制请求头
        builder.addInterceptor(chain -> {
            Request original = chain.request();
            Request modified = original.newBuilder()
                    .removeHeader("Accept-Encoding") // 移除OkHttp自动添加的gzip
                    .build();
            return chain.proceed(modified);
        });
        
        this.client = builder.build();
    }

    public WebClient(Vertx vertx, WebClientOptions options) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(options.isFollowRedirects())
                .connectTimeout(options.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(options.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(options.getWriteTimeout(), TimeUnit.MILLISECONDS);

        // 禁用OkHttp的自动gzip处理，让客户端完全控制请求头
        builder.addInterceptor(chain -> {
            Request original = chain.request();
            Request modified = original.newBuilder()
                    .removeHeader("Accept-Encoding") // 移除OkHttp自动添加的gzip
                    .build();
            return chain.proceed(modified);
        });

        if (options.isUserAgentEnabled()) {
            builder.addInterceptor(chain -> {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();
                if (original.header("User-Agent") == null) {
                    requestBuilder.header("User-Agent", options.getUserAgent());
                }
                return chain.proceed(requestBuilder.build());
            });
        }

        this.client = builder.build();
    }

    public static WebClient create() {
        return new WebClient();
    }

    public static WebClient create(Vertx vertx) {
        return new WebClient();
    }

    public static WebClient create(Vertx vertx, WebClientOptions options) {
        return new WebClient(vertx, options);
    }

    // 默认返回Buffer类型（底层数据）
    public RequestBuilder<Buffer> getAbs(String url) {
        return new RequestBuilder<>(client, "GET", url);
    }

    public RequestBuilder<Buffer> getAbs(io.vertx.uritemplate.UriTemplate template) {
        return new RequestBuilder<>(client, "GET", template != null ? template.toString() : "");
    }

    public RequestBuilder<Buffer> postAbs(String url) {
        return new RequestBuilder<>(client, "POST", url);
    }

    public RequestBuilder<Buffer> postAbs(io.vertx.uritemplate.UriTemplate template) {
        return new RequestBuilder<>(client, "POST", template != null ? template.toString() : "");
    }

    public RequestBuilder<Buffer> putAbs(String url) {
        return new RequestBuilder<>(client, "PUT", url);
    }

    public RequestBuilder<Buffer> putAbs(io.vertx.uritemplate.UriTemplate template) {
        return new RequestBuilder<>(client, "PUT", template != null ? template.toString() : "");
    }

    public RequestBuilder<Buffer> deleteAbs(String url) {
        return new RequestBuilder<>(client, "DELETE", url);
    }

    public RequestBuilder<Buffer> deleteAbs(io.vertx.uritemplate.UriTemplate template) {
        return new RequestBuilder<>(client, "DELETE", template != null ? template.toString() : "");
    }

    public RequestBuilder<Buffer> patchAbs(String url) {
        return new RequestBuilder<>(client, "PATCH", url);
    }

    public RequestBuilder<Buffer> patchAbs(io.vertx.uritemplate.UriTemplate template) {
        return new RequestBuilder<>(client, "PATCH", template != null ? template.toString() : "");
    }

    public RequestBuilder<Buffer> headAbs(String url) {
        return new RequestBuilder<>(client, "HEAD", url);
    }

    public RequestBuilder<Buffer> headAbs(io.vertx.uritemplate.UriTemplate template) {
        return new RequestBuilder<>(client, "HEAD", template != null ? template.toString() : "");
    }

    public static class RequestBuilder<T> implements HttpRequest<T> {
        private final OkHttpClient client;
        private final String method;
        private String url;
        private final MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        private RequestBody body;
        private boolean followRedirects = false;
        private long timeout = 15000; // 15 seconds default
        private final MultiMap queryParams = MultiMap.caseInsensitiveMultiMap();
        @SuppressWarnings("rawtypes")
        private BodyCodec bodyCodec;

        public RequestBuilder(OkHttpClient client, String method, String url) {
            this.client = client;
            this.method = method;
            this.url = url;
            // 默认使用Buffer BodyCodec（底层数据）
            this.bodyCodec = BodyCodec.buffer();
        }
        
        /**
         * 设置响应体解码器
         * 注意：这里通过raw类型cast来绕过泛型限制，因为在运行时泛型会擦除
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public <U> RequestBuilder<U> as(BodyCodec<U> codec) {
            RequestBuilder rawBuilder = this;
            rawBuilder.bodyCodec = codec;
            return (RequestBuilder<U>) rawBuilder;
        }

        public RequestBuilder<T> putHeader(String key, String val) {
            headers.set(key, val);
            return this;
        }

        public RequestBuilder<T> putHeaders(MultiMap headers) {
            this.headers.putAll(headers);
            return this;
        }

        public RequestBuilder<T> sendJson(Object json) {
            MediaType jsonType = MediaType.get("application/json; charset=utf-8");
            body = RequestBody.create(json.toString(), jsonType);
            return this;
        }

        public Future<HttpResponse<T>> sendJsonObject(io.vertx.core.json.JsonObject jsonObject) {
            MediaType jsonType = MediaType.get("application/json; charset=utf-8");
            body = RequestBody.create(jsonObject.encode(), jsonType);
            return send();
        }

        public void sendJsonObject(io.vertx.core.json.JsonObject jsonObject, io.vertx.core.Handler<io.vertx.core.AsyncResult<HttpResponse<T>>> handler) {
            MediaType jsonType = MediaType.get("application/json; charset=utf-8");
            body = RequestBody.create(jsonObject.encode(), jsonType);
            send(handler);
        }

        public Future<HttpResponse<T>> sendBuffer(io.vertx.core.buffer.Buffer buffer) {
            body = RequestBody.create(buffer.getBytes());
            return send();
        }

        public void sendBuffer(io.vertx.core.buffer.Buffer buffer, io.vertx.core.Handler<io.vertx.core.AsyncResult<HttpResponse<T>>> handler) {
            body = RequestBody.create(buffer.getBytes());
            send(handler);
        }

        public Future<HttpResponse<T>> sendForm(MultiMap formData) {
            FormBody.Builder formBuilder = new FormBody.Builder();
            formData.forEach(formBuilder::add);
            body = formBuilder.build();
            return send();
        }

        public void sendForm(MultiMap formData, io.vertx.core.Handler<io.vertx.core.AsyncResult<HttpResponse<T>>> handler) {
            FormBody.Builder formBuilder = new FormBody.Builder();
            formData.forEach(formBuilder::add);
            body = formBuilder.build();
            send(handler);
        }

        public RequestBuilder<T> addQueryParam(String name, String value) {
            queryParams.set(name, value);
            return this;
        }

        public RequestBuilder<T> followRedirects(boolean follow) {
            this.followRedirects = follow;
            return this;
        }

        public RequestBuilder<T> timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public RequestBuilder<T> setTemplateParam(String name, String value) {
            // Handle UriTemplate parameters
            if (url.contains("{" + name + "}")) {
                url = url.replace("{" + name + "}", value);
            }
            return this;
        }

        public MultiMap queryParams() {
            return queryParams;
        }

        public RequestBuilder<T> sendMultipartForm(io.vertx.ext.web.multipart.MultipartForm form, io.vertx.core.Handler<io.vertx.core.AsyncResult<HttpResponse<T>>> handler) {
            MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);

            // Add form attributes
            form.getAttributes().forEach(multipartBuilder::addFormDataPart);

            // Add file uploads
            form.getFileUploads().forEach((name, fileUpload) -> {
                RequestBody fileBody = RequestBody.create(
                        fileUpload.buffer().getBytes(),
                        okhttp3.MediaType.parse(fileUpload.contentType())
                );
                multipartBuilder.addFormDataPart(name, fileUpload.filename(), fileBody);
            });

            body = multipartBuilder.build();
            return this;
        }

        public Future<HttpResponse<T>> send() {
            Future<HttpResponse<T>> future = new Future<>();
            executeRequest(future);
            return future;
        }

        public void send(io.vertx.core.Handler<io.vertx.core.AsyncResult<HttpResponse<T>>> handler) {
            executeRequest(null, handler);
        }

        private void executeRequest(Future<HttpResponse<T>> future) {
            executeRequest(future, null);
        }

        private void executeRequest(Future<HttpResponse<T>> future, io.vertx.core.Handler<io.vertx.core.AsyncResult<HttpResponse<T>>> handler) {
            // Create OkHttp client with custom settings
            OkHttpClient.Builder clientBuilder = client.newBuilder()
                    .followRedirects(followRedirects)
                    .callTimeout(timeout, java.util.concurrent.TimeUnit.MILLISECONDS);

            OkHttpClient customClient = clientBuilder.build();

            // Build URL with query parameters
            String finalUrl = url;
            if (!queryParams.isEmpty()) {
                StringBuilder urlBuilder = new StringBuilder(url);
                if (!url.contains("?")) {
                    urlBuilder.append("?");
                } else {
                    urlBuilder.append("&");
                }
                
                boolean first = true;
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    if (!first) {
                        urlBuilder.append("&");
                    }
                    urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                    first = false;
                }
                finalUrl = urlBuilder.toString();
            }

            Request.Builder rb = new Request.Builder().url(finalUrl);
            headers.forEach(rb::addHeader);

            // Set request method and body
            switch (method.toUpperCase()) {
                case "POST":
                    rb.post(body != null ? body : RequestBody.create(new byte[0]));
                    break;
                case "PUT":
                    rb.put(body != null ? body : RequestBody.create(new byte[0]));
                    break;
                case "PATCH":
                    rb.patch(body != null ? body : RequestBody.create(new byte[0]));
                    break;
                case "DELETE":
                    if (body != null) {
                        rb.delete(body);
                    } else {
                        rb.delete();
                    }
                    break;
                case "HEAD":
                    rb.head();
                    break;
                case "GET":
                default:
                    rb.get();
                    break;
            }

            customClient.newCall(rb.build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (future != null) {
                        future.fail(e);
                    }
                    if (handler != null) {
                        handler.handle(new io.vertx.core.AsyncResultImpl<HttpResponse<T>>(e));
                    }
                }

                @Override
                public void onResponse(Call call, Response res) throws IOException {
                    MultiMap responseHeaders = MultiMap.caseInsensitiveMultiMap();
                    res.headers().forEach(header -> 
                        responseHeaders.set(header.getFirst(), header.getSecond()));

                    // 获取响应体的字节数组
                    byte[] bodyBytes = null;
                    if (res.body() != null) {
                        bodyBytes = res.body().bytes();
                    }
                    
                    // OkHttp的行为：自动解压gzip，但可能对deflate和br的处理不一致
                    // 由于实际使用中都是通过asText()和asJson()来处理响应，这些方法会处理解压
                    // 所以我们不做任何特殊处理，统一让HttpResponseHelper来处理解压逻辑
                    
                    // 使用BodyCodec解码响应体
                    @SuppressWarnings("unchecked")
                    T decodedBody;
                    if (bodyCodec.getTargetClass() == Buffer.class) {
                        // Buffer类型：直接创建Buffer
                        decodedBody = (T) Buffer.buffer(bodyBytes != null ? bodyBytes : new byte[0]);
                    } else {
                        // String或其他类型：先转为String再解码
                        String bodyString = new String(bodyBytes != null ? bodyBytes : new byte[0], StandardCharsets.UTF_8);
                        decodedBody = (T) bodyCodec.decode(bodyString, responseHeaders);
                    }
                    
                    // 创建响应对象
                    HttpResponse<T> response = createResponse(res.code(), decodedBody, responseHeaders);
                    
                    if (future != null) {
                        future.complete(response);
                    }
                    if (handler != null) {
                        handler.handle(new io.vertx.core.AsyncResultImpl<>(response));
                    }
                }
            });
        }
        
        /**
         * 创建响应对象
         */
        private HttpResponse<T> createResponse(int code, T body, MultiMap headers) {
            return new HttpResponse<>(code, body, headers);
        }
    }
}