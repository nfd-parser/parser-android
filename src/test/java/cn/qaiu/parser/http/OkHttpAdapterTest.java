package cn.qaiu.parser.http;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class OkHttpAdapterTest {
    private MockWebServer mockWebServer;
    private WebClient client;
    private String baseUrl;

    @Before
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseUrl = mockWebServer.url("/").toString();
        client = WebClient.create();
    }

    @After
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testGetRequest() throws InterruptedException {
        // 准备模拟响应
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("test response")
            .addHeader("Content-Type", "text/plain"));

        CountDownLatch latch = new CountDownLatch(1);
        
        // 发送请求
        Future<HttpResponse<Buffer>> future = client.getAbs(baseUrl)
            .putHeader("User-Agent", "OkHttpAdapter/1.0")
            .putHeader("Accept", "text/plain")
            .send();

        future.onSuccess(response -> {
            assertEquals(200, response.statusCode());
            assertEquals("test response", response.bodyAsString());
            latch.countDown();
        });

        future.onFailure(error -> {
            fail("Request should not fail: " + error.getMessage());
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // 验证请求
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("OkHttpAdapter/1.0", request.getHeader("User-Agent"));
        assertEquals("text/plain", request.getHeader("Accept"));
    }

    @Test
    public void testPostRequest() throws InterruptedException {
        // 准备模拟响应
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(201)
            .setBody("{\"status\":\"created\"}")
            .addHeader("Content-Type", "application/json"));

        CountDownLatch latch = new CountDownLatch(1);
        String jsonBody = "{\"test\":\"data\"}";

        // 发送请求
        Future<HttpResponse<Buffer>> future = client.postAbs(baseUrl)
            .putHeader("Content-Type", "application/json")
            .sendJson(jsonBody)
            .send();

        future.onSuccess(response -> {
            assertEquals(201, response.statusCode());
            assertEquals("{\"status\":\"created\"}", response.bodyAsString());
            latch.countDown();
        });

        future.onFailure(error -> {
            fail("Request should not fail: " + error.getMessage());
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // 验证请求
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertTrue(request.getHeader("Content-Type").startsWith("application/json"));
        assertEquals(jsonBody, request.getBody().readUtf8());
    }

    @Test
    public void testErrorHandling() throws InterruptedException {
        // 准备错误响应
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("Not Found"));

        CountDownLatch latch = new CountDownLatch(1);

        // 发送请求
        Future<HttpResponse<Buffer>> future = client.getAbs(baseUrl).send();

        future.onSuccess(response -> {
            assertEquals(404, response.statusCode());
            assertEquals("Not Found", response.bodyAsString());
            latch.countDown();
        });

        future.onFailure(error -> {
            fail("Should handle 404 as success with error status code");
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testNetworkError() throws InterruptedException {
        // 使用一个不存在的地址
        String invalidUrl = "http://invalid.example.com";
        CountDownLatch latch = new CountDownLatch(1);

        Future<HttpResponse<Buffer>> future = client.getAbs(invalidUrl).send();

        future.onSuccess(response -> {
            fail("Should fail for invalid URL");
            latch.countDown();
        });

        future.onFailure(error -> {
            assertNotNull(error);
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testLargeResponse() throws InterruptedException {
        // 创建大响应体
        StringBuilder largeBody = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeBody.append("Line ").append(i).append("\n");
        }

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(largeBody.toString()));

        CountDownLatch latch = new CountDownLatch(1);

        Future<HttpResponse<Buffer>> future = client.getAbs(baseUrl).send();

        future.onSuccess(response -> {
            assertEquals(200, response.statusCode());
            assertEquals(largeBody.length(), response.bodyAsString().length());
            latch.countDown();
        });

        future.onFailure(error -> {
            fail("Should handle large response body");
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testMultipleRequests() throws InterruptedException {
        int requestCount = 5;
        CountDownLatch latch = new CountDownLatch(requestCount);

        for (int i = 0; i < requestCount; i++) {
            final int index = i;
            mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("Response " + index));

            Future<HttpResponse<Buffer>> future = client.getAbs(baseUrl).send();

            future.onSuccess(response -> {
                assertEquals(200, response.statusCode());
                assertEquals("Response " + index, response.bodyAsString());
                latch.countDown();
            });

            future.onFailure(error -> {
                fail("Request " + index + " failed: " + error.getMessage());
                latch.countDown();
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertEquals(requestCount, mockWebServer.getRequestCount());
    }

    @Test
    public void testRequestTimeout() throws InterruptedException {
        // 设置延迟响应
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("Delayed Response")
            .setHeadersDelay(2, TimeUnit.SECONDS)
            .setBodyDelay(2, TimeUnit.SECONDS));

        CountDownLatch latch = new CountDownLatch(1);

        // 使用较短的超时时间
        WebClient clientWithTimeout = WebClient.create();
        Future<HttpResponse<Buffer>> future = clientWithTimeout.getAbs(baseUrl).send();

        future.onSuccess(response -> {
            fail("Should timeout");
            latch.countDown();
        });

        future.onFailure(error -> {
            assertTrue(error instanceof IOException);
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testRedirectHandling() throws InterruptedException {
        // 设置重定向响应
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(302)
            .addHeader("Location", baseUrl + "redirected"));
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("Redirected Content"));

        CountDownLatch latch = new CountDownLatch(1);

        Future<HttpResponse<Buffer>> future = client.getAbs(baseUrl).send();

        future.onSuccess(response -> {
            // 由于我们配置了不自动跟随重定向，应该收到 302 响应
            assertEquals(302, response.statusCode());
            assertEquals(baseUrl + "redirected", response.getHeader("Location"));
            latch.countDown();
        });

        future.onFailure(error -> {
            fail("Should handle redirect response");
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}