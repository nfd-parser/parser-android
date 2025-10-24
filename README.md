# parser-android

NFD 解析器模块（Android兼容版）：聚合各类网盘/分享页解析，统一输出文件列表与下载信息，供上层下载器使用。

- 语言：Java 17
- 构建：Maven
- 模块版本：10.2.1
- **Android兼容**：使用OkHttp替代Vert.x，完美支持Android平台

## 依赖（Maven Central）
```xml
<dependency>
  <groupId>cn.qaiu</groupId>
  <artifactId>parser-android</artifactId>
  <version>10.2.1</version>
</dependency>
```
- Gradle Groovy DSL：
```groovy
dependencies {
  implementation 'cn.qaiu:parser-android:10.2.1'
}
```
- Gradle Kotlin DSL：
```kotlin
dependencies {
  implementation("cn.qaiu:parser-android:10.2.1")
}
```

## Android 兼容性说明

### OkHttp 替代 Vert.x
本项目专门为Android平台优化，使用OkHttp替代了Vert.x作为HTTP客户端：

- **OkHttp 5.2.0**：现代化的HTTP客户端，完美支持Android
- **异步处理**：基于CompletableFuture，与Android异步编程模式兼容
- **网络优化**：支持HTTP/2、连接池、缓存等现代网络特性
- **轻量级**：相比Vert.x更轻量，适合移动端应用

### Android 集成优势
1. **无额外依赖**：OkHttp是Android生态的标准HTTP库
2. **性能优化**：针对移动网络环境优化
3. **内存友好**：减少不必要的线程和资源消耗
4. **兼容性好**：支持Android 5.0+（API 21+）

### 使用示例（Android）
```java
// Android 环境下的使用方式
CompletableFuture<List<FileInfo>> future = ParserCreate
    .fromShareUrl("https://share.feijipan.com/s/3pMsofZd")
    .createTool()
    .parseFileList()
    .toCompletionStage().toCompletableFuture();

// 在Android主线程中处理结果
future.thenAccept(fileList -> {
    // 更新UI
    runOnUiThread(() -> {
        adapter.updateData(fileList);
    });
}).exceptionally(throwable -> {
    // 处理错误
    Log.e("Parser", "解析失败", throwable);
    return null;
});
```

## 核心 API 速览
- **OkHttpClient**：内置OkHttp客户端，无需额外配置
- **ParserCreate**：从分享链接或类型构建解析器；生成短链 path
- **IPanTool**：统一解析接口（parse、parseFileList、parseById）
- **CustomParserRegistry**：自定义解析器注册中心（支持扩展）
- **CustomParserConfig**：自定义解析器配置类（支持扩展）

## 使用示例（极简）
```java
// 同步方式
List<FileInfo> list = ParserCreate
  .fromShareUrl("https://share.feijipan.com/s/3pMsofZd")
  .createTool()
  .parseFileList()
  .toCompletionStage().toCompletableFuture().join();

// 异步方式（推荐Android使用）
CompletableFuture<List<FileInfo>> future = ParserCreate
  .fromShareUrl("https://share.feijipan.com/s/3pMsofZd")
  .createTool()
  .parseFileList()
  .toCompletionStage().toCompletableFuture();
```
完整示例与调试脚本见 doc/README.md。

## 快速开始
- 环境：JDK >= 17，Maven >= 3.9
- 构建/安装：
```bash
mvn clean package -DskipTests
mvn install
```
- 测试：
```bash
mvn test
```

### Android 项目集成
在Android项目的 `build.gradle` 中添加：
```gradle
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
    implementation 'cn.qaiu:parser-android:10.2.1'
    // OkHttp 已包含，无需额外添加
}
```

## 自定义解析器扩展
本模块支持用户自定义解析器扩展。通过简单的配置和注册，你可以添加自己的网盘解析实现：

```java
// 1. 继承 PanBase 抽象类（推荐）
public class MyPanTool extends PanBase {
    public MyPanTool(ShareLinkInfo info) { 
        super(info);
    }
    @Override
    public Future<String> parse() { 
        // 使用 PanBase 提供的 OkHttp 客户端
        Request request = new Request.Builder()
            .url("https://api.example.com")
            .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    complete(extractUrlFromJson(json));
                } else {
                    handleFail("请求失败: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call call, IOException e) {
                handleFail("网络错误: " + e.getMessage());
            }
        });
        return future();
    }
}

// 2. 注册到系统
CustomParserConfig config = CustomParserConfig.builder()
    .type("mypan")
    .displayName("我的网盘")
    .toolClass(MyPanTool.class)
    .build();
CustomParserRegistry.register(config);

// 3. 使用自定义解析器（仅支持 fromType 方式）
IPanTool tool = ParserCreate.fromType("mypan")
    .shareKey("abc123")
    .createTool();
String url = tool.parseSync();
```

**详细文档：** [自定义解析器扩展指南](doc/CUSTOM_PARSER_GUIDE.md)

## 文档
- doc/README.md：解析约定、示例、IDEA `.http` 调试
- **doc/CUSTOM_PARSER_GUIDE.md：自定义解析器扩展完整指南**
- **doc/ANDROID_MIGRATION_GUIDE.md：Android迁移指南**
- **doc/WEBCLIENT_ANDROID_COMPATIBILITY.md：OkHttp Android兼容性说明**

## 目录结构
- src/main/java/cn/qaiu/entity：通用实体（如 FileInfo）
- src/main/java/cn/qaiu/parser：解析框架 & 各站点实现（impl）
- src/main/java/cn/qaiu/util：工具类（OkHttp相关工具）
- src/test/java：单测与示例
- src/main/resources/custom-parsers：自定义解析器脚本

## 许可证
MIT License
