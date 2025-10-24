package cn.qaiu.parser.test;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SimplePanRequestTest {
    
    @Test
    public void testBasicHttpRequest() throws InterruptedException {
        WebClient client = WebClient.create();
        CountDownLatch latch = new CountDownLatch(1);

        Future<io.vertx.ext.web.client.HttpResponse<Buffer>> future = client
            .getAbs("https://httpbin.org/get")
            .putHeader("User-Agent", "Parser-Android/1.0")
            .send();

        future.onSuccess(response -> {
            System.out.println("Status: " + response.statusCode());
            System.out.println("Body: " + response.bodyAsString());
            assertEquals(200, response.statusCode());
            assertNotNull(response.bodyAsString());
            latch.countDown();
        });

        future.onFailure(error -> {
            System.err.println("Request failed: " + error.getMessage());
            fail("Request should not fail: " + error.getMessage());
            latch.countDown();
        });

        assertTrue("Request should complete within 10 seconds", latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testPostRequest() throws InterruptedException {
        WebClient client = WebClient.create();
        CountDownLatch latch = new CountDownLatch(1);

        Future<io.vertx.ext.web.client.HttpResponse<Buffer>> future = client
            .postAbs("https://httpbin.org/post")
            .putHeader("Content-Type", "application/json")
            .sendJson("{\"test\":\"data\"}")
            .send();

        future.onSuccess(response -> {
            System.out.println("POST Status: " + response.statusCode());
            System.out.println("POST Body: " + response.bodyAsString());
            assertEquals(200, response.statusCode());
            assertNotNull(response.bodyAsString());
            latch.countDown();
        });

        future.onFailure(error -> {
            System.err.println("POST Request failed: " + error.getMessage());
            fail("POST Request should not fail: " + error.getMessage());
            latch.countDown();
        });

        assertTrue("POST Request should complete within 10 seconds", latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testErrorHandling() throws InterruptedException {
        WebClient client = WebClient.create();
        CountDownLatch latch = new CountDownLatch(1);

        Future<io.vertx.ext.web.client.HttpResponse<Buffer>> future = client
            .getAbs("https://httpbin.org/status/404")
            .send();

        future.onSuccess(response -> {
            System.out.println("Error Status: " + response.statusCode());
            assertEquals(404, response.statusCode());
            latch.countDown();
        });

        future.onFailure(error -> {
            System.err.println("Error Request failed: " + error.getMessage());
            fail("Should handle 404 as success with error status code");
            latch.countDown();
        });

        assertTrue("Error Request should complete within 10 seconds", latch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testConcurrentRequests() throws InterruptedException {
        WebClient client = WebClient.create();
        int requestCount = 3;
        CountDownLatch latch = new CountDownLatch(requestCount);

        for (int i = 0; i < requestCount; i++) {
            final int index = i;
            Future<io.vertx.ext.web.client.HttpResponse<Buffer>> future = client
                .getAbs("https://httpbin.org/delay/1")
                .send();

            future.onSuccess(response -> {
                System.out.println("Concurrent request " + index + " completed with status: " + response.statusCode());
                assertEquals(200, response.statusCode());
                latch.countDown();
            });

            future.onFailure(error -> {
                System.err.println("Concurrent request " + index + " failed: " + error.getMessage());
                fail("Concurrent request " + index + " should not fail");
                latch.countDown();
            });
        }

        assertTrue("All concurrent requests should complete within 15 seconds", latch.await(15, TimeUnit.SECONDS));
    }
}