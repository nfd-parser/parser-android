# Parser Android 迁移指南

本指南将帮助您从标准的 parser 模块迁移到 Android 兼容版本。

## 概述

parser-android 是 parser 的 Android 兼容版本，它使用 OkHttp 替代了 Vert.x WebClient，同时保持了完全的 API 兼容性。这意味着您可以在 Android 项目中使用相同的代码，只需更改依赖项即可。

## 依赖配置

### Maven

```xml
<dependency>
    <groupId>cn.qaiu</groupId>
    <artifactId>parser-android</artifactId>
    <version>10.2.1</version>
</dependency>
```

### Gradle

```gradle
implementation 'cn.qaiu:parser-android:10.2.1'
```

## 主要变更

1. 移除了 Vert.x 依赖
2. 使用 OkHttp 替代 WebClient
3. 实现了兼容层以保持 API 一致性
4. 使用 Rhino 替代 Nashorn JavaScript 引擎

## 兼容性说明

### 保持不变的部分

- 所有 parser 接口和类名
- 异步操作 API（Future/Promise）
- HTTP 客户端 API
- 现有的解析器实现
- JavaScript 解析器支持

### 实现差异

虽然 API 保持一致，但底层实现有所不同：

1. 异步操作基于 Java CompletableFuture
2. HTTP 请求基于 OkHttp
3. 移除了 Vert.x 事件循环
4. JavaScript 引擎使用 Rhino 替代 Nashorn

## 已实现的 Vert.x 兼容类库

### 核心类库
- `io.vertx.core.Future` - 异步操作 Future
- `io.vertx.core.Promise` - 异步操作 Promise
- `io.vertx.core.Handler` - 事件处理器接口
- `io.vertx.core.AsyncResult` - 异步结果接口
- `io.vertx.core.MultiMap` - 多值映射
- `io.vertx.core.buffer.Buffer` - 缓冲区
- `io.vertx.core.WorkerExecutor` - 工作线程执行器
- `io.vertx.core.Vertx` - Vert.x 核心类

### JSON 处理
- `io.vertx.core.json.JsonObject` - JSON 对象
- `io.vertx.core.json.JsonArray` - JSON 数组
- `io.vertx.core.json.pointer.JsonPointer` - JSON 指针

### HTTP 客户端
- `io.vertx.ext.web.client.WebClient` - HTTP 客户端
- `io.vertx.ext.web.client.HttpRequest` - HTTP 请求接口
- `io.vertx.ext.web.client.HttpResponse` - HTTP 响应
- `io.vertx.ext.web.client.WebClientOptions` - 客户端配置
- `io.vertx.ext.web.client.WebClientSession` - 会话管理

### 网络和代理
- `io.vertx.core.net.ProxyOptions` - 代理配置
- `io.vertx.core.http.impl.headers.VertxHttpHeaders` - HTTP 头部

### 多部分表单
- `io.vertx.ext.web.multipart.MultipartForm` - 多部分表单

### URI 模板
- `io.vertx.uritemplate.UriTemplate` - URI 模板

### JavaScript 引擎兼容
- `org.openjdk.nashorn.api.scripting.ScriptObjectMirror` - JavaScript 对象镜像

## 使用示例

```java
// 创建解析器
QQwTool tool = new QQwTool(shareLinkInfo);

// 异步解析
tool.parse()
    .onSuccess(result -> {
        // 处理结果
    })
    .onFailure(error -> {
        // 处理错误
    });

// 同步解析
String result = tool.parseSync();
```

## 性能考虑

1. 内存使用
   - parser-android 版本内存占用更小
   - 没有事件循环开销

2. 启动时间
   - 启动更快，无需初始化 Vert.x

3. 并发处理
   - 使用 OkHttp 连接池
   - 基于系统线程池

4. JavaScript 执行
   - Rhino 引擎性能良好
   - 支持所有 JavaScript 解析器功能

## 故障排除

### 常见问题

1. 编译错误
   ```
   问题：找不到 io.vertx 包
   解决：确保使用了正确的 parser-android 依赖
   ```

2. 运行时异常
   ```
   问题：android.os.NetworkOnMainThreadException
   解决：确保在后台线程中执行网络操作
   ```

3. JavaScript 执行问题
   ```
   问题：JavaScript 解析器无法正常工作
   解决：确保 Rhino 依赖正确加载
   ```

### 最佳实践

1. 在 Android 中使用时，建议：
   - 使用 Kotlin 协程包装异步操作
   - 利用 Android 生命周期管理请求
   - 实现适当的错误处理和重试机制

2. 性能优化：
   - 复用 WebClient 实例
   - 适当配置超时和重试策略
   - 实现请求缓存

3. JavaScript 解析器：
   - 预加载常用的 JavaScript 解析器
   - 缓存编译后的脚本
   - 避免频繁的脚本重新编译

## 支持

如果您在迁移过程中遇到问题，请：

1. 查看项目文档
2. 提交 Issue
3. 联系技术支持

## 版本历史

- 10.2.1
  - 初始 Android 兼容版本
  - 完整的 Vert.x API 兼容层
  - OkHttp 实现
  - Rhino JavaScript 引擎支持
  - 完整的类库适配
