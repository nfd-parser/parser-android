package cn.qaiu.parser.impl;

import cn.qaiu.entity.ShareLinkInfo;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class QQwToolTest {
    private QQwTool tool;
    private ShareLinkInfo shareLinkInfo;
    private Vertx vertx;

    @Before
    public void setup() {
        vertx = Vertx.vertx();
        shareLinkInfo = new ShareLinkInfo.Builder()
            .shareUrl("https://example.com/test-share")
            .build();
        tool = new QQwTool(shareLinkInfo);
    }
    
    @After
    public void tearDown() {
        if (vertx != null) {
            vertx.close();
        }
    }

    @Test
    public void testParse() throws InterruptedException {
        Future<String> future = tool.parse();
        
        future.onSuccess(result -> {
            assertNotNull(result);
        });
        
        future.onFailure(error -> {
            fail("Should not fail: " + error.getMessage());
        });

        assertTrue("解析应在超时内完成", Vertx.awaitTermination(5));
    }

    @Test
    public void testParseSync() {
        try {
            String result = tool.parseSync();
            assertNotNull(result);
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }
}