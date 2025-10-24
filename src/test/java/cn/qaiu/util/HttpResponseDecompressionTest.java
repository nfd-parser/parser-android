package cn.qaiu.util;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * HttpResponse解压功能真实HTTP请求测试
 */
public class HttpResponseDecompressionTest {

    @Test
    public void testRealGzipResponse() throws InterruptedException {
        System.out.println("======= 测试真实Gzip压缩响应 =======");
        
        WebClient client = WebClient.create();
        CountDownLatch latch = new CountDownLatch(1);
        
        // 使用一个返回gzip压缩内容的网站
        String url = "https://httpbin.org/gzip";
        
        System.out.println("请求URL: " + url);
        System.out.println("预期: 返回gzip压缩的JSON响应");
        System.out.println();
        
        Future<HttpResponse<Buffer>> future = client.getAbs(url).send();
        
        future.onSuccess(response -> {
            System.out.println("状态码: " + response.statusCode());
            System.out.println("Content-Encoding: " + response.getHeader("Content-Encoding"));
            System.out.println();
            
            // 使用asText获取解压后的文本
            String text = HttpResponseHelper.asText(response);
            
            System.out.println("解压后的内容: " + text);
            System.out.println();
            
            // 验证内容
            assertNotNull("响应内容不应为空", text);
            assertTrue("应包含JSON内容", text.contains("gzipped"));
            assertFalse("不应包含乱码", text.contains("�"));
            
            System.out.println("✓ 解压成功！内容正常");
            latch.countDown();
        });
        
        future.onFailure(error -> {
            System.err.println("✗ 请求失败: " + error.getMessage());
            error.printStackTrace();
            latch.countDown();
        });
        
        assertTrue("应在10秒内完成", latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testRealDeflateResponse() throws InterruptedException {
        System.out.println("======= 测试真实Deflate压缩响应 =======");
        
        WebClient client = WebClient.create();
        CountDownLatch latch = new CountDownLatch(1);
        
        // 使用一个返回deflate压缩内容的网站
        String url = "https://httpbin.org/deflate";
        
        System.out.println("请求URL: " + url);
        System.out.println("预期: 返回deflate压缩的JSON响应");
        System.out.println();
        
        Future<HttpResponse<Buffer>> future = client.getAbs(url).send();
        
        future.onSuccess(response -> {
            System.out.println("状态码: " + response.statusCode());
            System.out.println("Content-Encoding: " + response.getHeader("Content-Encoding"));
            System.out.println();
            
            // 使用asText获取解压后的文本
            String text = HttpResponseHelper.asText(response);
            
            System.out.println("解压后的内容: " + text);
            System.out.println();
            
            // 验证内容
            assertNotNull("响应内容不应为空", text);
            assertTrue("应包含JSON内容", text.contains("deflated"));
            assertFalse("不应包含乱码", text.contains("�"));
            
            System.out.println("✓ 解压成功！内容正常");
            latch.countDown();
        });
        
        future.onFailure(error -> {
            System.err.println("✗ 请求失败: " + error.getMessage());
            error.printStackTrace();
            latch.countDown();
        });
        
        assertTrue("应在10秒内完成", latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testRealBrotliResponse() throws InterruptedException {
        System.out.println("======= 测试真实Brotli压缩响应 =======");
        
        WebClient client = WebClient.create();
        CountDownLatch latch = new CountDownLatch(1);
        
        // 使用一个返回brotli压缩内容的网站
        String url = "https://httpbin.org/brotli";
        
        System.out.println("请求URL: " + url);
        System.out.println("预期: 返回brotli压缩的JSON响应");
        System.out.println();
        
        Future<HttpResponse<Buffer>> future = client.getAbs(url).send();
        
        future.onSuccess(response -> {
            System.out.println("状态码: " + response.statusCode());
            System.out.println("Content-Encoding: " + response.getHeader("Content-Encoding"));
            System.out.println();
            
            // 使用asText获取解压后的文本
            String text = HttpResponseHelper.asText(response);
            
            System.out.println("解压后的内容: " + text);
            System.out.println();
            
            // 验证内容
            assertNotNull("响应内容不应为空", text);
            assertTrue("应包含JSON内容", text.contains("brotli"));
            assertFalse("不应包含乱码", text.contains("�"));
            
            System.out.println("✓ 解压成功！内容正常");
            latch.countDown();
        });
        
        future.onFailure(error -> {
            System.err.println("✗ 请求失败: " + error.getMessage());
            error.printStackTrace();
            latch.countDown();
        });
        
        assertTrue("应在10秒内完成", latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testRealChineseWebsite() throws InterruptedException {
        System.out.println("======= 测试真实中文网站 (验证中文不乱码) =======");
        
        WebClient client = WebClient.create();
        CountDownLatch latch = new CountDownLatch(1);
        
        // 使用一个返回中文内容的网站
        String url = "https://www.baidu.com";
        
        System.out.println("请求URL: " + url);
        System.out.println("预期: 返回包含中文的HTML");
        System.out.println();
        
        Future<HttpResponse<Buffer>> future = client.getAbs(url).send();
        
        future.onSuccess(response -> {
            System.out.println("状态码: " + response.statusCode());
            System.out.println("Content-Encoding: " + response.getHeader("Content-Encoding"));
            System.out.println();
            
            // 使用asText获取解压后的文本
            String text = HttpResponseHelper.asText(response);
            
            if (text != null && text.length() > 0) {
                System.out.println("响应内容前200字符: " + text.substring(0, Math.min(200, text.length())));
                System.out.println();
                
                // 验证中文是否正常
                assertTrue("应包含中文", text.contains("百度") || text.contains("html"));
                assertFalse("不应包含乱码", text.contains("�"));
                
                System.out.println("✓ 中文显示正常！无乱码");
            } else {
                System.out.println("响应内容为空");
            }
            
            latch.countDown();
        });
        
        future.onFailure(error -> {
            System.err.println("✗ 请求失败: " + error.getMessage());
            error.printStackTrace();
            latch.countDown();
        });
        
        assertTrue("应在10秒内完成", latch.await(10, TimeUnit.SECONDS));
    }
}

