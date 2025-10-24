package io.vertx.ext.web.client;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

public class WebClientTest {
    private WebClient client;

    @Test
    public void testRequestBuilder() {
        client = WebClient.create();
        assertNotNull(client);

        WebClient.RequestBuilder getBuilder = client.getAbs("http://example.com");
        assertNotNull(getBuilder);

        WebClient.RequestBuilder postBuilder = client.postAbs("http://example.com");
        assertNotNull(postBuilder);
    }

    @Test
    public void testRequestHeaders() {
        client = WebClient.create();
        WebClient.RequestBuilder builder = client.getAbs("http://example.com")
            .putHeader("test-header", "test-value");

        assertNotNull(builder);
    }

    @Test
    public void testJsonRequest() {
        client = WebClient.create();
        WebClient.RequestBuilder builder = client.postAbs("http://example.com")
            .putHeader("Content-Type", "application/json")
            .sendJson("{\"test\":\"data\"}");

        assertNotNull(builder);
    }

    @Test
    public void testFutureHandling() throws InterruptedException {
        client = WebClient.create();
        CountDownLatch latch = new CountDownLatch(1);

        Future<HttpResponse<Buffer>> future = client.getAbs("http://example.com").send();
        
        future.onSuccess(response -> {
            latch.countDown();
        });
        
        future.onFailure(error -> {
            latch.countDown();
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}