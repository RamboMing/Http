package com.rambo.httplib.request;

import com.rambo.httplib.cache.Cache;
import com.rambo.httplib.core.DefaultRetryPolicy;
import com.rambo.httplib.core.RetryPolicy;


import java.util.HashMap;
import java.util.Map;

import okhttp3.CookieJar;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public abstract class RequestBuilder {
    public static final int DEFAULT_TIMEOUT_MS = 10000;
    protected int method;
    protected String url;
    protected String redirectUrl;
    protected boolean shouldCache = true;
    protected boolean shouldFromCache = false;
    protected boolean shouldOfflineCache = false;
    protected boolean forceCache = false;
    protected int maxCacheAage = 0;
    protected RetryPolicy retryPolicy = new DefaultRetryPolicy();
    protected Object tag;
    protected Cache cookieCache;
    protected HttpParams params = new HttpParams();
    protected int trafficStatsTag;
    protected Class rspClass;
    protected int connTimeoutMs = 10000;
    protected int readTimeoutMs = 10000;
    protected int writeTimeoutMs = 10000;
    protected String storePath;
    protected CookieJar cookieJar;
    protected Map<String, String> header = new HashMap();
    protected Cache cache;

    public RequestBuilder() {
    }

    public abstract Request build();

    public abstract RequestBuilder method(int var1);

    public abstract RequestBuilder url(String var1);

    public abstract RequestBuilder redirectUrl(String var1);

    public abstract RequestBuilder shouldCache(boolean var1);

    public abstract RequestBuilder shouldFromCache(boolean var1);

    public abstract RequestBuilder forceCache(boolean var1);

    public abstract RequestBuilder maxCacheAage(int var1);

    public abstract RequestBuilder retryPolicy(RetryPolicy var1);

    public abstract RequestBuilder tag(Object var1);

    public abstract RequestBuilder params(HttpParams var1);

    public abstract RequestBuilder addUrlParams(String var1, String var2);

    public abstract RequestBuilder addFileParams(String var1, String var2);

    public abstract RequestBuilder trafficStatsTag(int var1);

    public abstract RequestBuilder rspClass(Class var1);

    public abstract RequestBuilder writeTimeoutMs(int var1);

    public abstract RequestBuilder readTimeoutMs(int var1);

    public abstract RequestBuilder connTimeoutMs(int var1);

    public abstract RequestBuilder storePath(String var1);

    public abstract RequestBuilder cookieJar(CookieJar var1);

    public abstract RequestBuilder header(Map<String, String> var1);

    public abstract RequestBuilder addHeader(String var1, String var2);
}

