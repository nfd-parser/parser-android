package io.vertx.ext.web.client;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public interface HttpRequest<T> {
    HttpRequest<T> putHeader(String name, String value);
    HttpRequest<T> putHeaders(MultiMap headers);
    HttpRequest<T> sendJson(Object body);
    Future<HttpResponse<T>> sendJsonObject(JsonObject jsonObject);
    void sendJsonObject(JsonObject jsonObject, Handler<AsyncResult<HttpResponse<T>>> handler);
    Future<HttpResponse<T>> sendBuffer(Buffer buffer);
    void sendBuffer(Buffer buffer, Handler<AsyncResult<HttpResponse<T>>> handler);
    Future<HttpResponse<T>> sendForm(MultiMap formData);
    void sendForm(MultiMap formData, Handler<AsyncResult<HttpResponse<T>>> handler);
    HttpRequest<T> addQueryParam(String name, String value);
    HttpRequest<T> followRedirects(boolean follow);
    Future<HttpResponse<T>> send();
    void send(Handler<AsyncResult<HttpResponse<T>>> handler);
    HttpRequest<T> timeout(long timeout);
    HttpRequest<T> setTemplateParam(String name, String value);
    MultiMap queryParams();
}