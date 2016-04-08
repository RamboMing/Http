package com.rambo.httplib.response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class NetworkResponse {
    public final int statusCode;
    public final byte[] data;
    public final Map<String, String> headers;
    public final boolean notModified;
    public final long networkTimeMs;
    private long contentLength;
    private InputStream contentStream;

    public NetworkResponse(int statusCode, long contentLength, byte[] data, Map<String, String> headers, boolean notModified, long networkTimeMs) {
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
        this.notModified = notModified;
        this.networkTimeMs = networkTimeMs;
        this.contentLength = contentLength;
    }

    public NetworkResponse(int statusCode, long contentLength, byte[] data, Map<String, String> headers, boolean notModified) {
        this(statusCode, contentLength, data, headers, notModified, 0L);
    }

    public NetworkResponse(long contentLength, byte[] data) {
        this(200, contentLength, data, (Map)Collections.emptyMap(), false, 0L);
    }

    public NetworkResponse(long contentLength, byte[] data, Map<String, String> headers) {
        this(200, contentLength, data, headers, false, 0L);
    }

    public InputStream getContentStream() {
        return new ByteArrayInputStream(this.data);
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }
}
