package com.rambo.httplib.request;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import com.rambo.httplib.cache.Cache;
import com.rambo.httplib.core.RequestQueue;
import com.rambo.httplib.core.RetryPolicy;
import com.rambo.httplib.exception.HttpException;
import com.rambo.httplib.response.HttpResponseListnener;
import com.rambo.httplib.response.NetworkResponse;
import com.rambo.httplib.response.Response;
import com.rambo.httplib.response.URLHttpResponse;
import com.rambo.httplib.utils.ByteArrayPool;
import com.rambo.httplib.utils.HttpLog;
import com.rambo.httplib.utils.IOUtils;
import com.rambo.httplib.utils.InternalUtils;
import com.rambo.httplib.utils.PoolingByteArrayOutputStream;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public  abstract class Request<T> implements Comparable<Request<T>> {
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";
    protected static final String PROTOCOL_CHARSET = "utf-8";
    private final HttpLog.MarkerLog mEventLog;
    private final int mMethod;
    private final String mUrl;
    protected HttpParams mParams;
    private String mRedirectUrl;
    private String mIdentifier;
    private final int mDefaultTrafficStatsTag;
    private Integer mSequence;
    protected RequestQueue mRequestQueue;
    private boolean mShouldCache;
    private boolean mShouldFromCache;
    private boolean mforceCache;
    private int maxCacheAage;
    private Cache cookieCache;
    private boolean mCanceled;
    private boolean mResponseDelivered;
    private long mRequestBirthTime;
    private static final long SLOW_REQUEST_THRESHOLD_MS = 3000L;
    private RetryPolicy mRetryPolicy;
    private Cache.Entry mCacheEntry;
    private Object mTag;
    protected HttpResponseListnener mListener;
    protected Class<T> rspClazz;
    private int connTimeoutMs;
    private int readTimeoutMs;
    private int writeTimeoutMs;
    private String storePath;
    private CookieJar cookieJar;
    protected Map<String, String> header;
    private static long sCounter;

    public Request(RequestBuilder builder) {
        this.mEventLog = HttpLog.MarkerLog.ENABLED ? new HttpLog.MarkerLog() : null;
        this.mShouldCache = true;
        this.mShouldFromCache = false;
        this.mforceCache = false;
        this.maxCacheAage = 0;
        this.mCanceled = false;
        this.mResponseDelivered = false;
        this.mRequestBirthTime = 0L;
        this.mCacheEntry = null;
        this.mMethod = builder.method;
        this.mUrl = builder.url;
        this.mRedirectUrl = builder.redirectUrl;
        this.mShouldCache = builder.shouldCache;
        this.mShouldFromCache = builder.shouldFromCache;
        this.mforceCache = builder.forceCache;
        this.maxCacheAage = builder.maxCacheAage;
        this.mRetryPolicy = builder.retryPolicy;
        this.mTag = builder.tag;
        this.cookieCache = builder.cookieCache;
        this.mParams = builder.params;
        this.mDefaultTrafficStatsTag = builder.trafficStatsTag;
        this.rspClazz = builder.rspClass;
        this.connTimeoutMs = builder.connTimeoutMs;
        this.readTimeoutMs = builder.readTimeoutMs;
        this.writeTimeoutMs = builder.writeTimeoutMs;
        this.storePath = builder.storePath;
        this.cookieJar = builder.cookieJar;
        this.header = builder.header;
    }

    public int getMethod() {
        return this.mMethod;
    }

    public Object getTag() {
        return this.mTag;
    }

    public int getTrafficStatsTag() {
        return this.mDefaultTrafficStatsTag;
    }

    private static int findDefaultTrafficStatsTag(String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            if (uri != null) {
                String host = uri.getHost();
                if (host != null) {
                    return host.hashCode();
                }
            }
        }

        return 0;
    }

    public void addMarker(String tag) {
        if (HttpLog.MarkerLog.ENABLED) {
            this.mEventLog.add(tag, Thread.currentThread().getId());
        } else if (this.mRequestBirthTime == 0L) {
            this.mRequestBirthTime = SystemClock.elapsedRealtime();
        }

    }

    public void finish(final String tag) {
        if (this.mRequestQueue != null) {
            this.mRequestQueue.finish(this);
        }

        final long requestTime;
        if (HttpLog.MarkerLog.ENABLED) {
            requestTime = Thread.currentThread().getId();
            if (Looper.myLooper() != Looper.getMainLooper()) {
                Handler mainThread = new Handler(Looper.getMainLooper());
                mainThread.post(new Runnable() {
                    public void run() {
                        Request.this.mEventLog.add(tag, requestTime);
                        Request.this.mEventLog.finish(this.toString());
                    }
                });
                return;
            }

            this.mEventLog.add(tag, requestTime);
            this.mEventLog.finish(this.toString());
        } else {
            requestTime = SystemClock.elapsedRealtime() - this.mRequestBirthTime;
            if (requestTime >= 3000L) {
                HttpLog.d("%d ms: %s", new Object[]{Long.valueOf(requestTime), this.toString()});
            }
        }

    }

    public Request<?> setRequestQueue(RequestQueue requestQueue) {
        this.mRequestQueue = requestQueue;
        return this;
    }

    public final Request<?> setSequence(int sequence) {
        this.mSequence = Integer.valueOf(sequence);
        return this;
    }

    public final int getSequence() {
        if (this.mSequence == null) {
            throw new IllegalStateException("getSequence called before setSequence");
        } else {
            return this.mSequence.intValue();
        }
    }

    public String getUrl() {
        return this.mRedirectUrl != null ? this.mRedirectUrl : this.mUrl;
    }

    public String getOriginUrl() {
        return this.mUrl;
    }

    public String getIdentifier() {
        return this.mIdentifier;
    }

    public String getCacheKey() {
        return this.getUrl();
    }

    public Request<?> setCacheEntry(Cache.Entry entry) {
        this.mCacheEntry = entry;
        return this;
    }

    public Cache.Entry getCacheEntry() {
        return this.mCacheEntry;
    }

    public void cancel() {
        this.mCanceled = true;
    }

    public boolean isCanceled() {
        return this.mCanceled;
    }

    public Map<String, String> getHeaders() {
        if (this.cookieCache != null) {
            this.cookieCache.initialize();
            String cookieKey = this.getCookieKey();
            Cache.Entry entry = this.cookieCache.get(cookieKey);
            HashMap cookieHeader = new HashMap();
            if (entry != null) {
                Map cookieHeaders = entry.responseHeaders;
                if (cookieHeaders != null) {
                    String token = (String) cookieHeaders.get("token");
                    if (!TextUtils.isEmpty(token)) {
                        cookieHeader.put("token", token);
                    }

                    String sessionid = (String) cookieHeaders.get("SESSION");
                    if (!TextUtils.isEmpty(sessionid)) {
                        cookieHeader.put("Cookie", "SESSION=" + sessionid + "; Path=/;");
                    }

                    return cookieHeader;
                }
            }
        }

        return Collections.emptyMap();
    }

    public String getCookieKey() {
        try {
            URL e = new URL(this.mUrl);
            InetAddress inetAddress = InetAddress.getByName(e.getHost());
            String hostName = inetAddress.getCanonicalHostName();
            return "cookie";
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }

    protected String getParamsEncoding() {
        return "UTF-8";
    }

    public RequestBody getBody() {
        return null;
    }

    public final boolean shouldCache() {
        return this.mShouldCache;
    }

    public boolean shouldFromCache() {
        return this.mShouldFromCache;
    }

    public boolean isForcache() {
        return this.mforceCache;
    }

    public int getMaxCacheAage() {
        return this.maxCacheAage;
    }

    public Request.Priority getPriority() {
        return Request.Priority.NORMAL;
    }

    public final int getTimeoutMs() {
        return this.connTimeoutMs;
    }

    public RetryPolicy getRetryPolicy() {
        return this.mRetryPolicy;
    }

    public void markDelivered() {
        this.mResponseDelivered = true;
    }

    public boolean hasHadResponseDelivered() {
        return this.mResponseDelivered;
    }

    public abstract Response<T> parseNetworkResponse(NetworkResponse var1);

    public HttpException parseNetworkError(HttpException volleyError) {
        return volleyError;
    }

    public void deliverResponse(boolean isCache, Map<String, String> headers, T response) {
        if (this.mListener != null) {
            this.mListener.onSuccess(isCache, headers, response);
        }

    }

    public byte[] entityToBytes(URLHttpResponse httpResponse) throws IOException, HttpException {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(ByteArrayPool.get(), (int) httpResponse.getContentLength());
        byte[] buffer = null;

        byte[] var9;
        try {
            InputStream in = httpResponse.getContentStream();
            if (in == null) {
                throw new HttpException(1104);
            }

            buffer = ByteArrayPool.get().getBuf(1024);
            long contentLength = httpResponse.getContentLength();
            int transferredBytes = 0;

            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
                transferredBytes += count;
                if (this.mListener != null) {
                    this.mRequestQueue.getDelivery().postProgress(this.mListener, (long) transferredBytes, contentLength);
                }
            }

            var9 = bytes.toByteArray();
        } finally {
            IOUtils.closeIO(new Closeable[]{httpResponse.getContentStream()});
            ByteArrayPool.get().returnBuf(buffer);
            IOUtils.closeIO(new Closeable[]{bytes});
        }

        return var9;
    }

    public void deliverError(HttpException error) {
        if (this.mListener != null) {
            this.mListener.onFailure(error);
        }

    }

    public void deliverStart() {
        if (this.mListener != null) {
            (new Handler(Looper.getMainLooper())).post(new Runnable() {
                public void run() {
                    Request.this.mListener.onStart();
                }
            });
        }

    }

    public int compareTo(Request<T> other) {
        Request.Priority left = this.getPriority();
        Request.Priority right = other.getPriority();
        return left == right ? this.mSequence.intValue() - other.mSequence.intValue() : right.ordinal() - left.ordinal();
    }

    public String toString() {
        String trafficStatsTag = "0x" + Integer.toHexString(this.getTrafficStatsTag());
        return (this.mCanceled ? "[X] " : "[ ] ") + this.getUrl() + " " + trafficStatsTag + " " + this.getPriority() + " " + this.mSequence;
    }

    private static String createIdentifier(int method, String url) {
        return InternalUtils.sha1Hash("Request:" + method + ":" + url + ":" + System.currentTimeMillis() + ":" + sCounter++);
    }

    public Cache getCookieCache() {
        return this.cookieCache;
    }

    public RequestQueue getRequestQueue() {
        return this.mRequestQueue;
    }

    public HttpResponseListnener getListener() {
        return this.mListener;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.mRedirectUrl = redirectUrl;
    }

    public HttpParams getHttpParams() {
        return this.mParams;
    }

    public int getReadTimeoutMs() {
        return this.readTimeoutMs;
    }

    public int getWriteTimeoutMs() {
        return this.writeTimeoutMs;
    }

    public String getStorePath() {
        return this.storePath;
    }

    public CookieJar getCookieJar() {
        return this.cookieJar != null ? this.cookieJar : CookieJar.NO_COOKIES;
    }

    public void setListener(HttpResponseListnener listener) {
        this.mListener = listener;
    }

    protected void addParams(FormBody.Builder builder) {
        Map urlParams = this.mParams.getUrlParams();
        Iterator var3 = urlParams.keySet().iterator();

        while (var3.hasNext()) {
            String key = (String) var3.next();
            builder.add(key, (String) urlParams.get(key));
        }

    }

    protected void addParams(okhttp3.MultipartBody.Builder builder) {
        Map urlParams = this.mParams.getUrlParams();
        if (urlParams != null && !urlParams.isEmpty()) {
            Iterator var3 = urlParams.keySet().iterator();

            while (var3.hasNext()) {
                String key = (String) var3.next();
                builder.addPart(Headers.of(new String[]{"Content-Disposition", "form-data; name=\"" + key + "\""}), RequestBody.create((MediaType) null, (String) urlParams.get(key)));
            }
        }

    }

    public static enum Priority {
        LOW,
        NORMAL,
        HIGH,
        IMMEDIATE;

        private Priority() {
        }
    }

    public interface Method {
        int DEPRECATED_GET_OR_POST = -1;
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }
}
