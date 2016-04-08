package com.rambo.httplib.core;

import android.os.SystemClock;

import com.rambo.httplib.cache.Cache;
import com.rambo.httplib.exception.HttpException;
import com.rambo.httplib.request.Request;
import com.rambo.httplib.response.NetworkResponse;
import com.rambo.httplib.response.URLHttpResponse;
import com.rambo.httplib.utils.ByteArrayPool;
import com.rambo.httplib.utils.HttpLog;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.cookie.DateUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class BasicNetwork implements Network {
    protected static final boolean DEBUG;
    private static int SLOW_REQUEST_THRESHOLD_MS;
    private static int DEFAULT_POOL_SIZE;
    protected final HttpStack mHttpStack;
    protected final ByteArrayPool mPool;

    public BasicNetwork(HttpStack httpStack) {
        this(httpStack, new ByteArrayPool(DEFAULT_POOL_SIZE));
    }

    public BasicNetwork(HttpStack httpStack, ByteArrayPool pool) {
        this.mHttpStack = httpStack;
        this.mPool = pool;
    }

    public NetworkResponse performRequest(Request<?> request) throws HttpException {
        long requestStart = SystemClock.elapsedRealtime();

        while (true) {
            URLHttpResponse httpResponse = null;
            byte[] responseContents = null;
            Map responseHeaders = Collections.emptyMap();

            int statusCode1;
            try {
                HashMap e = new HashMap();
                this.addCacheHeaders(e, request.getCacheEntry());
                httpResponse = this.mHttpStack.performRequest(request, e);
                statusCode1 = httpResponse.getResponseCode();
                long contentLength = httpResponse.getContentLength();
                HashMap responseHeaders1 = httpResponse.getHeaders();
                if (statusCode1 == 304) {
                    Cache.Entry newUrl1 = request.getCacheEntry();
                    if (newUrl1 == null) {
                        return new NetworkResponse(304, contentLength, (byte[]) null, responseHeaders1, true, SystemClock.elapsedRealtime() - requestStart);
                    }

                    newUrl1.responseHeaders.putAll(responseHeaders1);
                    return new NetworkResponse(304, contentLength, newUrl1.data, newUrl1.responseHeaders, true, SystemClock.elapsedRealtime() - requestStart);
                }

                if (statusCode1 == 301 || statusCode1 == 302) {
                    String newUrl = (String) responseHeaders1.get("Location");
                    request.setRedirectUrl(newUrl);
                }

                if (httpResponse.getContentStream() != null) {
                    responseContents = request.entityToBytes(httpResponse);
                }

                if (responseContents == null) {
                    responseContents = new byte[0];
                }

                if (statusCode1 >= 200 && statusCode1 <= 299) {
                    return new NetworkResponse(statusCode1, contentLength, responseContents, responseHeaders1, false, SystemClock.elapsedRealtime() - requestStart);
                }

                throw new IOException();
            } catch (SocketTimeoutException var12) {
                attemptRetryOnException(request, new HttpException(1102));
            } catch (ConnectTimeoutException var13) {
                attemptRetryOnException(request, new HttpException(1102));
            } catch (MalformedURLException var14) {
                throw new RuntimeException("Bad URL " + request.getUrl(), var14);
            } catch (IOException var15) {
                boolean statusCode = false;
                if (httpResponse == null) {
                    throw new HttpException(1101);
                }

                statusCode1 = httpResponse.getResponseCode();
                if (statusCode1 == 1101) {
                    throw new HttpException(404);
                }

                if (responseContents == null) {
                    throw new HttpException(1101);
                }

                if (statusCode1 != 401 && statusCode1 != 403 && statusCode1 != 301 && statusCode1 != 302) {
                    throw new HttpException(statusCode1);
                }

                attemptRetryOnException(request, new HttpException(statusCode1));
            }
        }
    }

    private boolean isZip(HttpEntity entity) {
        Header ceheader = entity.getContentEncoding();
        if (ceheader != null) {
            HeaderElement[] var3 = ceheader.getElements();
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                HeaderElement element = var3[var5];
                if (element.getName().equalsIgnoreCase("gzip")) {
                    return true;
                }
            }
        }

        return false;
    }

    public byte[] unzip(byte[] data) {
        try {
            ByteArrayInputStream e = new ByteArrayInputStream(data);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            decompress(e, baos);
            data = baos.toByteArray();
            baos.flush();
            baos.close();
            e.close();
            return data;
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public static void decompress(InputStream is, OutputStream os) throws Exception {
        GZIPInputStream gis = new GZIPInputStream(is);
        byte[] data = new byte[1024];

        int count;
        while ((count = gis.read(data, 0, 1024)) != -1) {
            os.write(data, 0, count);
        }

        gis.close();
    }

    private static void attemptRetryOnException(Request<?> request, HttpException exception) throws HttpException {
        RetryPolicy retryPolicy = request.getRetryPolicy();
        int oldTimeout = request.getTimeoutMs();

        try {
            retryPolicy.retry(exception);
        } catch (HttpException var5) {
            throw var5;
        }
    }

    private void addCacheHeaders(Map<String, String> headers, Cache.Entry entry) {
        if (entry != null) {
            if (entry.etag != null) {
                headers.put("If-None-Match", entry.etag);
            }

            if (entry.lastModified > 0L) {
                Date refTime = new Date(entry.lastModified);
                headers.put("If-Modified-Since", DateUtils.formatDate(refTime));
            }

        }
    }

    protected void logError(String what, String url, long start) {
        long now = SystemClock.elapsedRealtime();
        HttpLog.v("HTTP ERROR(%s) %d ms to fetch %s", new Object[]{what, Long.valueOf(now - start), url});
    }

    protected static Map<String, String> convertHeaders(Header[] headers) {
        TreeMap result = new TreeMap(String.CASE_INSENSITIVE_ORDER);

        for (int i = 0; i < headers.length; ++i) {
            result.put(headers[i].getName(), headers[i].getValue());
        }

        return result;
    }

    static {
        DEBUG = HttpLog.DEBUG;
        SLOW_REQUEST_THRESHOLD_MS = 3000;
        DEFAULT_POOL_SIZE = 4096;
    }
}
