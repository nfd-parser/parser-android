package cn.qaiu.parser.customjs;

import cn.qaiu.entity.FileInfo;
import cn.qaiu.entity.ShareLinkInfo;
import cn.qaiu.parser.IPanTool;
import cn.qaiu.parser.custom.CustomParserConfig;
import cn.qaiu.util.RhinoUtils;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.mozilla.javascript.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * JavaScript解析器执行器
 * 实现IPanTool接口，执行JavaScript解析器逻辑
 *
 * @author <a href="https://qaiu.top">QAIU</a>
 * Create at 2025/10/17
 */
public class JsParserExecutor implements IPanTool {
    
    private static final Logger log = LoggerFactory.getLogger(JsParserExecutor.class);
    
    private final CustomParserConfig config;
    private final ShareLinkInfo shareLinkInfo;
    private final Context context;
    private final Scriptable scope;
    private final JsHttpClient httpClient;
    private final JsLogger jsLogger;
    private final JsShareLinkInfoWrapper shareLinkInfoWrapper;
    private final Promise<String> promise = Promise.promise();
    
    public JsParserExecutor(ShareLinkInfo shareLinkInfo, CustomParserConfig config) {
        this.config = config;
        this.shareLinkInfo = shareLinkInfo;
        
        // 检查是否有代理配置
        JsonObject proxyConfig = null;
        if (shareLinkInfo.getOtherParam().containsKey("proxy")) {
            proxyConfig = (JsonObject) shareLinkInfo.getOtherParam().get("proxy");
        }
        
        this.httpClient = new JsHttpClient(proxyConfig);
        this.jsLogger = new JsLogger("JsParser-" + config.getType());
        this.shareLinkInfoWrapper = new JsShareLinkInfoWrapper(shareLinkInfo);
        
        // 初始化引擎（必须在httpClient等对象创建后才能初始化）
        ContextHolder holder = initEngine();
        this.context = holder.context;
        this.scope = holder.scope;
    }
    
    /**
     * 获取ShareLinkInfo对象
     * @return ShareLinkInfo对象
     */
    public ShareLinkInfo getShareLinkInfo() {
        return shareLinkInfo;
    }
    
    /**
     * 用于保存Context和Scriptable的内部类
     */
    private static class ContextHolder {
        final Context context;
        final Scriptable scope;
        
        ContextHolder(Context context, Scriptable scope) {
            this.context = context;
            this.scope = scope;
        }
    }
    
    /**
     * 初始化JavaScript引擎 - 使用Rhino
     */
    private ContextHolder initEngine() {
        try {
            Context context = ContextFactory.getGlobal().enterContext();
            Scriptable scope = context.initStandardObjects();
            
            // 注入Java对象到JavaScript环境
            scope.put("http", scope, Context.javaToJS(httpClient, scope));
            scope.put("logger", scope, Context.javaToJS(jsLogger, scope));
            scope.put("shareLinkInfo", scope, Context.javaToJS(shareLinkInfoWrapper, scope));
            
            // 执行JavaScript代码
            context.evaluateString(scope, config.getJsCode(), config.getType(), 1, null);
            
            log.debug("JavaScript引擎初始化成功，解析器类型: {}", config.getType());
            return new ContextHolder(context, scope);
            
        } catch (Exception e) {
            log.error("JavaScript引擎初始化失败", e);
            throw new RuntimeException("JavaScript引擎初始化失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Future<String> parse() {
        try {
            jsLogger.info("开始执行JavaScript解析器: {}", config.getType());
            
            // 获取parse函数
            Object parseFunction = scope.get("parse", scope);
            if (parseFunction == null || parseFunction instanceof Undefined) {
                throw new RuntimeException("JavaScript代码中未找到parse函数");
            }
            
            if (parseFunction instanceof Function) {
                // 转换参数为JavaScript值
                Object[] args = new Object[]{
                    Context.javaToJS(shareLinkInfoWrapper, scope),
                    Context.javaToJS(httpClient, scope),
                    Context.javaToJS(jsLogger, scope)
                };
                
                // 调用函数
                Object result = ((Function) parseFunction).call(context, scope, scope, args);
                
                if (result instanceof String) {
                    jsLogger.info("解析成功: {}", result);
                    promise.complete((String) result);
                } else if (result instanceof NativeJavaObject) {
                    String javaString = RhinoUtils.toJavaString(result);
                    jsLogger.info("解析成功: {}", javaString);
                    promise.complete(javaString);
                } else {
                    jsLogger.error("parse方法返回值类型错误，期望String，实际: {}", 
                            result != null ? result.getClass().getSimpleName() : "null");
                    promise.fail("parse方法返回值类型错误");
                }
            } else {
                throw new RuntimeException("parse函数类型错误");
            }
            
        } catch (Exception e) {
            jsLogger.error("JavaScript解析器执行失败", e);
            promise.fail("JavaScript解析器执行失败: " + e.getMessage());
        }
        
        return promise.future();
    }
    
    @Override
    public Future<List<FileInfo>> parseFileList() {
        Promise<List<FileInfo>> promise = Promise.promise();
        
        try {
            jsLogger.info("开始执行JavaScript文件列表解析: {}", config.getType());
            
            // 获取parseFileList函数
            Object parseFileListFunction = scope.get("parseFileList", scope);
            if (parseFileListFunction == null || parseFileListFunction instanceof Undefined) {
                throw new RuntimeException("JavaScript代码中未找到parseFileList函数");
            }
            
            // 调用parseFileList方法
            if (parseFileListFunction instanceof Function) {
                Object[] args = new Object[]{
                    Context.javaToJS(shareLinkInfoWrapper, scope),
                    Context.javaToJS(httpClient, scope),
                    Context.javaToJS(jsLogger, scope)
                };
                
                Object result = ((Function) parseFileListFunction).call(context, scope, scope, args);
                
                if (result instanceof NativeArray) {
                    List<FileInfo> fileList = convertToFileInfoList((NativeArray) result);
                    
                    jsLogger.info("文件列表解析成功，共 {} 个文件", fileList.size());
                    promise.complete(fileList);
                } else {
                    jsLogger.error("parseFileList方法返回值类型错误，期望数组，实际: {}", 
                            result != null ? result.getClass().getSimpleName() : "null");
                    promise.fail("parseFileList方法返回值类型错误");
                }
            } else {
                throw new RuntimeException("parseFileList函数类型错误");
            }
            
        } catch (Exception e) {
            jsLogger.error("JavaScript文件列表解析失败", e);
            promise.fail("JavaScript文件列表解析失败: " + e.getMessage());
        }
        
        return promise.future();
    }
    
    @Override
    public Future<String> parseById() {
        Promise<String> promise = Promise.promise();
        
        try {
            jsLogger.info("开始执行JavaScript按ID解析: {}", config.getType());
            
            // 获取parseById函数
            Object parseByIdFunction = scope.get("parseById", scope);
            if (parseByIdFunction == null || parseByIdFunction instanceof Undefined) {
                throw new RuntimeException("JavaScript代码中未找到parseById函数");
            }
            
            // 调用parseById方法
            if (parseByIdFunction instanceof Function) {
                Object[] args = new Object[]{
                    Context.javaToJS(shareLinkInfoWrapper, scope),
                    Context.javaToJS(httpClient, scope),
                    Context.javaToJS(jsLogger, scope)
                };
                
                Object result = ((Function) parseByIdFunction).call(context, scope, scope, args);
                
                if (result instanceof String) {
                    jsLogger.info("按ID解析成功: {}", result);
                    promise.complete((String) result);
                } else {
                    jsLogger.error("parseById方法返回值类型错误，期望String，实际: {}", 
                            result != null ? result.getClass().getSimpleName() : "null");
                    promise.fail("parseById方法返回值类型错误");
                }
            } else {
                throw new RuntimeException("parseById函数类型错误");
            }
            
        } catch (Exception e) {
            jsLogger.error("JavaScript按ID解析失败", e);
            promise.fail("JavaScript按ID解析失败: " + e.getMessage());
        }
        
        return promise.future();
    }
    
    /**
     * 将JavaScript对象数组转换为FileInfo列表
     */
    private List<FileInfo> convertToFileInfoList(NativeArray array) {
        List<FileInfo> fileList = new ArrayList<>();
        
        long length = array.getLength();
        for (long i = 0; i < length; i++) {
            Object item = array.get((int) i, array);
            if (item instanceof NativeObject) {
                FileInfo fileInfo = convertToFileInfo((NativeObject) item);
                if (fileInfo != null) {
                    fileList.add(fileInfo);
                }
            }
        }
        
        return fileList;
    }
    
    /**
     * 将JavaScript对象转换为FileInfo
     */
    private FileInfo convertToFileInfo(NativeObject itemObj) {
        try {
            FileInfo fileInfo = new FileInfo();
            
            // 设置基本字段
            Object fileName = itemObj.get("fileName", itemObj);
            if (fileName != null && !(fileName instanceof Undefined)) {
                fileInfo.setFileName(fileName.toString());
            }
            
            Object fileId = itemObj.get("fileId", itemObj);
            if (fileId != null && !(fileId instanceof Undefined)) {
                fileInfo.setFileId(fileId.toString());
            }
            
            Object fileType = itemObj.get("fileType", itemObj);
            if (fileType != null && !(fileType instanceof Undefined)) {
                fileInfo.setFileType(fileType.toString());
            }
            
            Object size = itemObj.get("size", itemObj);
            if (size != null && !(size instanceof Undefined) && size instanceof Number) {
                fileInfo.setSize(((Number) size).longValue());
            }
            
            Object sizeStr = itemObj.get("sizeStr", itemObj);
            if (sizeStr != null && !(sizeStr instanceof Undefined)) {
                fileInfo.setSizeStr(sizeStr.toString());
            }
            
            Object createTime = itemObj.get("createTime", itemObj);
            if (createTime != null && !(createTime instanceof Undefined)) {
                fileInfo.setCreateTime(createTime.toString());
            }
            
            Object updateTime = itemObj.get("updateTime", itemObj);
            if (updateTime != null && !(updateTime instanceof Undefined)) {
                fileInfo.setUpdateTime(updateTime.toString());
            }
            
            Object createBy = itemObj.get("createBy", itemObj);
            if (createBy != null && !(createBy instanceof Undefined)) {
                fileInfo.setCreateBy(createBy.toString());
            }
            
            Object downloadCount = itemObj.get("downloadCount", itemObj);
            if (downloadCount != null && !(downloadCount instanceof Undefined) && downloadCount instanceof Number) {
                fileInfo.setDownloadCount(((Number) downloadCount).intValue());
            }
            
            Object fileIcon = itemObj.get("fileIcon", itemObj);
            if (fileIcon != null && !(fileIcon instanceof Undefined)) {
                fileInfo.setFileIcon(fileIcon.toString());
            }
            
            Object panType = itemObj.get("panType", itemObj);
            if (panType != null && !(panType instanceof Undefined)) {
                fileInfo.setPanType(panType.toString());
            }
            
            Object parserUrl = itemObj.get("parserUrl", itemObj);
            if (parserUrl != null && !(parserUrl instanceof Undefined)) {
                fileInfo.setParserUrl(parserUrl.toString());
            }
            
            Object previewUrl = itemObj.get("previewUrl", itemObj);
            if (previewUrl != null && !(previewUrl instanceof Undefined)) {
                fileInfo.setPreviewUrl(previewUrl.toString());
            }
            
            return fileInfo;
            
        } catch (Exception e) {
            jsLogger.error("转换FileInfo对象失败", e);
            return null;
        }
    }
}
