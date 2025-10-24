package cn.qaiu.parser.impl;

import org.junit.After;
import org.junit.Test;

import cn.qaiu.entity.FileInfo;
import cn.qaiu.parser.ParserCreate;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 蓝奏云解析测试
 */
public class LzPanTest {
    
    private Vertx vertx;
    
    @After
    public void tearDown() {
        // 测试结束后关闭Vertx实例
        if (vertx != null) {
            vertx.close();
        }
    }

    @Test
    public void testLzDownload() throws InterruptedException {
        System.out.println("======= 蓝奏云解析测试 =======");
        
        // 测试链接（需要替换为真实的蓝奏云分享链接）
        // 示例格式: https://www.lanzoui.com/xxxxx 或 https://www.lanzoum.com/xxxxx
        String lzUrl = "https://www.lanzoui.com/xxx";  // 请替换为真实链接
        
        System.out.println("测试链接: " + lzUrl);
        System.out.println("支持的域名: lanzoui.com, lanzoum.com, lanzoux.com 等");
        System.out.println();
        
        try {
            // 创建Vertx实例（启动事件循环）
            vertx = Vertx.vertx();
            
            // 使用 ParserCreate 方式创建解析器
            ParserCreate parserCreate = ParserCreate.fromShareUrl(lzUrl);
            
            System.out.println("解析器类型: " + parserCreate.getShareLinkInfo().getType());
            System.out.println("网盘名称: " + parserCreate.getShareLinkInfo().getPanName());
            System.out.println("分享Key: " + parserCreate.getShareLinkInfo().getShareKey());
            System.out.println("标准URL: " + parserCreate.getShareLinkInfo().getStandardUrl());
            System.out.println();
            
            System.out.println("开始解析下载链接...");
            
            // 创建工具并解析
            io.vertx.core.Future<String> future = parserCreate.createTool().parse();
            future.onSuccess(downloadUrl -> {
                        System.out.println("✓ 解析成功!");
                        System.out.println("下载直链: " + downloadUrl);
                        System.out.println();
                        // 解析文件信息
                        FileInfo fileInfo = (FileInfo) parserCreate.getShareLinkInfo().getOtherParam().get("fileInfo");;
                        System.out.println("文件信息: " + fileInfo);
                        System.out.println();
                    });
            
            future.onFailure(error -> {
                        System.err.println("✗ 解析失败!");
                        System.err.println("错误信息: " + error.getMessage());
                        error.printStackTrace();
                    });
            
            // 等待异步结果（事件循环会自动保持程序运行）
            System.out.println("等待解析结果...");
            Vertx.awaitTermination(180);  // 仅用于测试等待
        } catch (Exception e) {
            System.err.println("创建解析器失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("======= 测试结束 =======");
    }
    
    @Test
    public void testLzWithShareKey() throws InterruptedException {
        System.out.println("======= 蓝奏云解析测试 (使用 shareKey) =======");
        
        // 创建Vertx实例（启动事件循环）
        vertx = Vertx.vertx();
        
        String shareKey = "xxxxx";
        
        System.out.println("分享Key: " + shareKey);
        System.out.println();
        
        // 使用 fromType + shareKey 方式
        ParserCreate parserCreate = ParserCreate.fromType("lz")
                .shareKey(shareKey);
        
        System.out.println("解析器类型: " + parserCreate.getShareLinkInfo().getType());
        System.out.println("网盘名称: " + parserCreate.getShareLinkInfo().getPanName());
        System.out.println("标准URL: " + parserCreate.getShareLinkInfo().getStandardUrl());
        System.out.println();
        
        System.out.println("开始解析下载链接...");
        
        // 创建工具并解析
        Future<String> future = parserCreate.createTool().parse();
        future.onSuccess(downloadUrl -> {
                    System.out.println("✓ 解析成功!");
                    System.out.println("下载直链: " + downloadUrl);
                    System.out.println();
                });
        
        future.onFailure(error -> {
                    System.err.println("✗ 解析失败!");
                    System.err.println("错误信息: " + error.getMessage());
                    error.printStackTrace();
                });
        
        // 等待异步结果（事件循环会自动保持程序运行）
        System.out.println("等待解析结果...");
        Vertx.awaitTermination(30);  // 仅用于测试等待

        System.out.println("======= 测试结束 =======");
    }
}

