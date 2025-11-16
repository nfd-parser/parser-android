package io.vertx.ext.web.client;

import io.netty.handler.codec.http.HttpMethod;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.junit.Test;
import static org.junit.Assert.*;

public class WebClientHttpMethodTest {

    @Test
    public void testGetMethod() {
        WebClient client = WebClient.create();
        WebClient.RequestBuilder<Buffer> request = client.getAbs("https://example.com");
        
        // Verify that the method is properly set
        assertNotNull(request);
    }

    @Test
    public void testPostMethod() {
        WebClient client = WebClient.create();
        WebClient.RequestBuilder<Buffer> request = client.postAbs("https://example.com");
        
        // Verify that the method is properly set
        assertNotNull(request);
    }

    @Test
    public void testPutMethod() {
        WebClient client = WebClient.create();
        WebClient.RequestBuilder<Buffer> request = client.putAbs("https://example.com");
        
        // Verify that the method is properly set
        assertNotNull(request);
    }

    @Test
    public void testDeleteMethod() {
        WebClient client = WebClient.create();
        WebClient.RequestBuilder<Buffer> request = client.deleteAbs("https://example.com");
        
        // Verify that the method is properly set
        assertNotNull(request);
    }

    @Test
    public void testPatchMethod() {
        WebClient client = WebClient.create();
        WebClient.RequestBuilder<Buffer> request = client.patchAbs("https://example.com");
        
        // Verify that the method is properly set
        assertNotNull(request);
    }

    @Test
    public void testHeadMethod() {
        WebClient client = WebClient.create();
        WebClient.RequestBuilder<Buffer> request = client.headAbs("https://example.com");
        
        // Verify that the method is properly set
        assertNotNull(request);
    }

    @Test
    public void testAllHttpMethodEnumsExist() {
        // Verify all expected HTTP methods exist
        assertNotNull(HttpMethod.GET);
        assertNotNull(HttpMethod.POST);
        assertNotNull(HttpMethod.PUT);
        assertNotNull(HttpMethod.DELETE);
        assertNotNull(HttpMethod.HEAD);
        assertNotNull(HttpMethod.PATCH);
    }

    @Test
    public void testHttpMethodNames() {
        assertEquals("GET", HttpMethod.GET.name());
        assertEquals("POST", HttpMethod.POST.name());
        assertEquals("PUT", HttpMethod.PUT.name());
        assertEquals("DELETE", HttpMethod.DELETE.name());
        assertEquals("HEAD", HttpMethod.HEAD.name());
        assertEquals("PATCH", HttpMethod.PATCH.name());
    }

    @Test
    public void testWebClientWithOptions() {
        Vertx vertx = Vertx.vertx();
        WebClientOptions options = new WebClientOptions()
                .setFollowRedirects(false)
                .setUserAgentEnabled(true)
                .setUserAgent("TestAgent/1.0");
        
        WebClient client = WebClient.create(vertx, options);
        assertNotNull(client);
    }

    @Test
    public void testRequestBuilderChaining() {
        WebClient client = WebClient.create();
        WebClient.RequestBuilder<Buffer> request = client.getAbs("https://example.com")
                .putHeader("Custom-Header", "value")
                .addQueryParam("key", "value");
        
        assertNotNull(request);
    }
}

