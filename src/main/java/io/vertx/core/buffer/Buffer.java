package io.vertx.core.buffer;

import java.nio.charset.Charset;

public class Buffer {
    private byte[] bytes;

    public static Buffer buffer(String string) {
        return new Buffer().appendString(string);
    }

    public static Buffer buffer(byte[] bytes) {
        Buffer buffer = new Buffer();
        buffer.bytes = bytes.clone();
        return buffer;
    }

    public Buffer appendString(String string) {
        if (bytes == null) {
            bytes = string.getBytes();
        } else {
            byte[] newBytes = new byte[bytes.length + string.getBytes().length];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            System.arraycopy(string.getBytes(), 0, newBytes, bytes.length, string.getBytes().length);
            bytes = newBytes;
        }
        return this;
    }

    public String toString(String encoding) {
        return new String(bytes); // Simplified
    }

    public String toString(Charset charset) {
        return new String(bytes, charset);
    }

    public String toString() {
        return new String(bytes); // Simplified
    }

    public byte[] getBytes() {
        return bytes != null ? bytes.clone() : new byte[0];
    }

    public int length() {
        return bytes != null ? bytes.length : 0;
    }
}