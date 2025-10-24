package io.vertx.ext.web.multipart.impl;

import io.vertx.ext.web.multipart.MultipartForm;

public class MultipartFormImpl extends MultipartForm {
    public static MultipartFormImpl create() {
        return new MultipartFormImpl();
    }
}
