# WebClient 链式调用兼容性改进总结

## 改进内容

为了完全兼容impl目录中解析器的使用方式，对WebClient的RequestBuilder进行了重要改进。

## 核心改进

### 1. sendJsonObject、sendForm、sendBuffer 自动发送 ✅

**改进前：**
```java
public RequestBuilder sendJsonObject(JsonObject jsonObject) {
    body = RequestBody.create(jsonObject.encode(), jsonType);
    return this;  // 返回RequestBuilder，需要手动调用send()
}
```

**改进后：**
```java
public Future<HttpResponse<String>> sendJsonObject(JsonObject jsonObject) {
    body = RequestBody.create(jsonObject.encode(), jsonType);
    return send();  // 自动发送并返回Future
}
```

### 2. 修改的方法

- `sendJsonObject(JsonObject jsonObject)` - 返回 `Future<HttpResponse<String>>`
- `sendForm(MultiMap formData)` - 返回 `Future<HttpResponse<String>>`
- `sendBuffer(Buffer buffer)` - 返回 `Future<HttpResponse<String>>`

### 3. HttpRequest接口更新

更新了HttpRequest接口的签名：
```java
public interface HttpRequest<T> {
    Future<HttpResponse<T>> sendJsonObject(JsonObject jsonObject);
    Future<HttpResponse<T>> sendBuffer(Buffer buffer);
    Future<HttpResponse<T>> sendForm(MultiMap formData);
    // ...
}
```

## 使用效果

### 改进前的使用方式
```java
client.postAbs(url)
    .sendJsonObject(json)
    .send()  // 需要手动调用send()
    .onSuccess(res -> { ... });
```

### 改进后的使用方式
```java
client.postAbs(url)
    .sendJsonObject(json)  // 自动发送
    .onSuccess(res -> { ... });
```

## 完全兼容的用法

现在支持所有impl目录中的使用方式：

```java
// WsTool.java - 支持直接链式调用
httpClient.postAbs(SHARE_URL_API + "login/anonymous")
    .putHeaders(headers)
    .sendJsonObject(JsonObject.of("dev_info", "{}"))
    .onSuccess(res -> { ... });

// YeTool.java - 支持sendJsonObject链式调用
client.postAbs(api)
    .setTemplateParam("authK", key)
    .setTemplateParam("authV", value)
    .sendJsonObject(jsonObject)
    .onSuccess(res2 -> { ... });

// LeTool.java - 支持sendJsonObject链式调用
client.postAbs(apiUrl)
    .sendJsonObject(JsonObject.of("shareId", dataKey, "password", pwd, "directoryId", -1))
    .onSuccess(res -> { ... });
```

## JsonObject.of 方法扩展

为了支持复杂的JSON对象创建，添加了更多的of方法重载：

- `of(key1, value1)` - 2个参数
- `of(key1, value1, key2, value2)` - 4个参数
- `of(key1, value1, ..., key8, value8)` - 16个参数
- 扩展到 `of(key1, value1, ..., key14, value14)` - 28个参数

支持WsTool中的复杂嵌套JSON对象创建：
```java
JsonObject.of(
    "start", 0,
    "sort", JsonObject.of("name", "asc"),
    "bid", filebid,
    "pid", filepid,
    "type", 1,
    "options", JsonObject.of("uploader", "true"),
    "size", 50
)
```

## QueryStringDecoder 完善

改进了QueryStringDecoder的实现：

1. **支持Charset参数**
   ```java
   public QueryStringDecoder(String uri, Charset charset)
   ```

2. **返回List<String>而非String**
   ```java
   public Map<String, List<String>> parameters()
   ```

3. **自动URL解码**
   - 使用URLDecoder自动解码参数
   - 支持UTF-8及其他字符集

## 兼容性保证

- ✅ 所有impl目录中的代码无需修改
- ✅ 完全支持链式调用
- ✅ sendJsonObject、sendForm、sendBuffer自动发送
- ✅ 编译成功，无错误

## 总结

通过这次改进，WebClient适配层现在完全兼容impl目录中所有解析器的使用方式：

1. **自动发送** - sendJsonObject等方法自动调用send()
2. **链式调用** - 支持直接链式调用onSuccess/onFailure
3. **JSON创建** - 支持多参数JsonObject.of方法
4. **查询参数解析** - 完善的QueryStringDecoder实现

所有解析器现在都可以在Android环境中正常工作，无需修改任何代码！
