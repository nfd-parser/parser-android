# WebClient 链式调用兼容性说明

根据现有代码的使用模式，需要在适配层中添加特殊的方法重载来支持直接链式调用。

## 问题分析

在Vert.x中，以下用法是正常的：
```java
client.postAbs(url).sendJsonObject(json).onSuccess(res -> { ... })
```

但在我们的适配层中，`sendJsonObject`返回`RequestBuilder`，不支持`.onSuccess()`。

## 解决方案

需要在`RequestBuilder`类中添加重载方法，当检测到链式调用时返回`Future`：

```java
// 原方法：返回RequestBuilder用于继续配置
public RequestBuilder sendJsonObject(JsonObject jsonObject) {
    body = RequestBody.create(jsonObject.encode(), jsonType);
    return this;
}

// 重载方法：返回Future用于链式调用
public Future<HttpResponse<String>> sendJsonObject(JsonObject jsonObject, Handler<AsyncResult<HttpResponse<String>>> handler) {
    body = RequestBody.create(jsonObject.encode(), jsonType);
    Future<HttpResponse<String>> future = send();
    if (handler != null) {
        future.onComplete(handler);
    }
    return future;
}
```

## 使用建议

建议在测试和实际使用中保持Vert.x的标准用法：
```java
client.postAbs(url)
    .sendJsonObject(json)
    .send()  // 显式调用send()
    .onSuccess(res -> { ... });
```

这样可以：
1. 更符合Vert.x的标准用法
2. 避免方法重载歧义
3. 保持代码的清晰性
