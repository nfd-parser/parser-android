package cn.qaiu.parser.impl;

import cn.qaiu.entity.ShareLinkInfo;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PanToolTest {
    private WebClient client;
    private ShareLinkInfo shareLinkInfo;
    private Vertx vertx;

    @Before
    public void setup() {
        vertx = Vertx.vertx();
        client = WebClient.create();
        shareLinkInfo = new ShareLinkInfo.Builder()
            .shareUrl("https://pan.baidu.com/s/1test")
            .build();
    }
    
    @After
    public void tearDown() {
        if (vertx != null) {
            vertx.close();
        }
    }

    @Test
    public void testBasicRequest() throws InterruptedException {
        Future<io.vertx.ext.web.client.HttpResponse<Buffer>> future = client
            .getAbs("https://httpbin.org/get")
            .putHeader("User-Agent", "Parser-Android/1.0")
            .send();

        future.onSuccess(response -> {
            assertEquals(200, response.statusCode());
            assertNotNull(response.bodyAsString());
        });

        future.onFailure(error -> {
            fail("Request should not fail: " + error.getMessage());
        });

        assertTrue("请求应在超时内完成", Vertx.awaitTermination(10));
    }

    @Test
    public void testPostRequest() throws InterruptedException {
        Future<io.vertx.ext.web.client.HttpResponse<Buffer>> future = client
            .postAbs("https://httpbin.org/post")
            .putHeader("Content-Type", "application/json")
            .sendJson("{\"test\":\"data\"}")
            .send();

        future.onSuccess(response -> {
            assertEquals(200, response.statusCode());
            assertNotNull(response.bodyAsString());
        });

        future.onFailure(error -> {
            fail("Request should not fail: " + error.getMessage());
        });

        assertTrue("请求应在超时内完成", Vertx.awaitTermination(10));
    }

    @Test
    public void testErrorHandling() throws InterruptedException {
        Future<io.vertx.ext.web.client.HttpResponse<Buffer>> future = client
            .getAbs("https://httpbin.org/status/404")
            .send();

        future.onSuccess(response -> {
            assertEquals(404, response.statusCode());
        });

        future.onFailure(error -> {
            fail("Should handle 404 as success with error status code");
        });

        assertTrue("请求应在超时内完成", Vertx.awaitTermination(10));
    }
}
