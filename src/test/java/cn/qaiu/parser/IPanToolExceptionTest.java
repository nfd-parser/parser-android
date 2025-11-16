package cn.qaiu.parser;

import cn.qaiu.entity.ShareLinkInfo;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.junit.Test;
import static org.junit.Assert.*;

public class IPanToolExceptionTest {

    @Test
    public void testParseSyncThrowsRuntimeException() {
        IPanTool tool = new IPanTool() {
            @Override
            public Future<String> parse() {
                Promise<String> promise = Promise.promise();
                promise.fail(new Exception("Parse error"));
                return promise.future();
            }
        };
        
        try {
            tool.parseSync();
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertNotNull(e);
            assertTrue(e.getMessage().contains("Parser execution failed"));
        }
    }

    @Test
    public void testParseSyncReturnsSuccess() {
        IPanTool tool = new IPanTool() {
            @Override
            public Future<String> parse() {
                Promise<String> promise = Promise.promise();
                promise.complete("success");
                return promise.future();
            }
        };
        
        String result = tool.parseSync();
        assertEquals("success", result);
    }

    @Test
    public void testParseFileListSyncThrowsRuntimeException() {
        IPanTool tool = new IPanTool() {
            @Override
            public Future<String> parse() {
                Promise<String> promise = Promise.promise();
                promise.complete("success");
                return promise.future();
            }
            
            @Override
            public Future<java.util.List<cn.qaiu.entity.FileInfo>> parseFileList() {
                Promise<java.util.List<cn.qaiu.entity.FileInfo>> promise = Promise.promise();
                promise.fail(new Exception("Parse error"));
                return promise.future();
            }
        };
        
        try {
            tool.parseFileListSync();
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertNotNull(e);
            assertTrue(e.getMessage().contains("Parser file list execution failed"));
        }
    }

    @Test
    public void testParseByIdSyncThrowsRuntimeException() {
        IPanTool tool = new IPanTool() {
            @Override
            public Future<String> parse() {
                Promise<String> promise = Promise.promise();
                promise.complete("success");
                return promise.future();
            }
            
            @Override
            public Future<String> parseById() {
                Promise<String> promise = Promise.promise();
                promise.fail(new Exception("Parse error"));
                return promise.future();
            }
        };
        
        try {
            tool.parseByIdSync();
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertNotNull(e);
            assertTrue(e.getMessage().contains("Parser ID execution failed"));
        }
    }

    @Test
    public void testParseWithClientLinksSyncThrowsRuntimeException() {
        ShareLinkInfo shareLinkInfo = ShareLinkInfo.newBuilder()
                .panName("test")
                .type("test")
                .shareUrl("https://test.com")
                .build();
        
        IPanTool tool = new IPanTool() {
            @Override
            public Future<String> parse() {
                Promise<String> promise = Promise.promise();
                promise.fail(new Exception("Parse error"));
                return promise.future();
            }
            
            @Override
            public ShareLinkInfo getShareLinkInfo() {
                return shareLinkInfo;
            }
        };
        
        try {
            tool.parseWithClientLinksSync();
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertNotNull(e);
            assertTrue(e.getMessage().contains("Parser client links execution failed"));
        }
    }

    @Test
    public void testParseSyncHandlesCheckedException() {
        IPanTool tool = new IPanTool() {
            @Override
            public Future<String> parse() {
                Promise<String> promise = Promise.promise();
                promise.fail(new java.io.IOException("IO error"));
                return promise.future();
            }
        };
        
        try {
            tool.parseSync();
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertNotNull(e);
            assertTrue(e.getCause() instanceof java.io.IOException);
        }
    }

    @Test
    public void testParseSyncPreservesRuntimeException() {
        IPanTool tool = new IPanTool() {
            @Override
            public Future<String> parse() {
                Promise<String> promise = Promise.promise();
                promise.fail(new IllegalArgumentException("Illegal argument"));
                return promise.future();
            }
        };
        
        try {
            tool.parseSync();
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Illegal argument", e.getMessage());
        }
    }
}

