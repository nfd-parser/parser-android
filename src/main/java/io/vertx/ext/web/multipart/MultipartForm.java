package io.vertx.ext.web.multipart;

import io.vertx.core.buffer.Buffer;
import java.util.HashMap;
import java.util.Map;

public class MultipartForm {
    private final Map<String, String> attributes = new HashMap<>();
    private final Map<String, FileUpload> fileUploads = new HashMap<>();

    public static MultipartForm create() {
        return new MultipartForm();
    }

    public MultipartForm setAttribute(String name, String value) {
        attributes.put(name, value);
        return this;
    }

    public MultipartForm attribute(String name, String value) {
        return setAttribute(name, value);
    }

    public MultipartForm setBinaryFileUpload(String name, String filename, String contentType, Buffer buffer) {
        fileUploads.put(name, new FileUpload(filename, contentType, buffer));
        return this;
    }

    public MultipartForm binaryFileUpload(String name, String filename, String contentType, Buffer buffer) {
        return setBinaryFileUpload(name, filename, contentType, buffer);
    }

    public Map<String, String> getAttributes() {
        return new HashMap<>(attributes);
    }

    public Map<String, FileUpload> getFileUploads() {
        return new HashMap<>(fileUploads);
    }

    public static class FileUpload {
        private final String filename;
        private final String contentType;
        private final Buffer buffer;

        public FileUpload(String filename, String contentType, Buffer buffer) {
            this.filename = filename;
            this.contentType = contentType;
            this.buffer = buffer;
        }

        public String filename() {
            return filename;
        }

        public String contentType() {
            return contentType;
        }

        public Buffer buffer() {
            return buffer;
        }
    }
}
