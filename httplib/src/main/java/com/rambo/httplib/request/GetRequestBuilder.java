package com.rambo.httplib.request;

import android.text.TextUtils;

import com.rambo.httplib.core.RetryPolicy;

import java.util.Map;

import okhttp3.CookieJar;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class GetRequestBuilder extends RequestBuilder {
    public GetRequestBuilder() {
    }

    public GetRequest build() {
        return new GetRequest(this);
    }

    public static GetRequestBuilder create() {
        return new GetRequestBuilder();
    }

    public GetRequestBuilder method(int method) {
        this.method = method;
        return this;
    }

    public GetRequestBuilder url(String url) {
        if(TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url == null");
        } else {
            this.url = url;
            return this;
        }
    }

    public GetRequestBuilder redirectUrl(String redirectUrl) {
        if(TextUtils.isEmpty(redirectUrl)) {
            throw new IllegalArgumentException("redirectUrl == null");
        } else {
            this.redirectUrl = redirectUrl;
            return this;
        }
    }

    public GetRequestBuilder shouldCache(boolean shouldCache) {
        this.shouldCache = shouldCache;
        return this;
    }

    public GetRequestBuilder shouldFromCache(boolean shouldFromCache) {
        this.shouldFromCache = shouldFromCache;
        return this;
    }

    public GetRequestBuilder forceCache(boolean forceCache) {
        this.forceCache = forceCache;
        return this;
    }

    public GetRequestBuilder maxCacheAage(int maxCacheAage) {
        if(maxCacheAage < 0) {
            maxCacheAage = 0;
        }

        this.maxCacheAage = maxCacheAage;
        return this;
    }

    public GetRequestBuilder retryPolicy(RetryPolicy retryPolicy) {
        if(retryPolicy == null) {
            return this;
        } else {
            this.retryPolicy = retryPolicy;
            return this;
        }
    }

    public GetRequestBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    public GetRequestBuilder params(HttpParams params) {
        if(params == null) {
            return this;
        } else {
            this.params = params;
            return this;
        }
    }

    public GetRequestBuilder addUrlParams(String key, String value) {
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            this.params.getUrlParams().put(key, value);
            return this;
        } else {
            return this;
        }
    }

    public GetRequestBuilder addFileParams(String key, String value) {
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            FormFile file = new FormFile(key, value);
            this.params.getFileParams().add(file);
            return this;
        } else {
            return this;
        }
    }

    public GetRequestBuilder trafficStatsTag(int trafficStatsTag) {
        this.trafficStatsTag = trafficStatsTag;
        return this;
    }

    public GetRequestBuilder rspClass(Class rspClass) {
        this.rspClass = rspClass;
        return this;
    }

    public GetRequestBuilder writeTimeoutMs(int writeTimeoutMs) {
        this.writeTimeoutMs = writeTimeoutMs;
        return this;
    }

    public GetRequestBuilder readTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
        return this;
    }

    public GetRequestBuilder connTimeoutMs(int connTimeoutMs) {
        this.connTimeoutMs = connTimeoutMs;
        return this;
    }

    public GetRequestBuilder storePath(String storePath) {
        return this;
    }

    public GetRequestBuilder cookieJar(CookieJar cookieJar) {
        if(cookieJar == null) {
            cookieJar = CookieJar.NO_COOKIES;
        }

        this.cookieJar = cookieJar;
        return this;
    }

    public GetRequestBuilder header(Map<String, String> headers) {
        if(headers != null && !headers.isEmpty()) {
            this.header = headers;
            return this;
        } else {
            return this;
        }
    }

    public GetRequestBuilder addHeader(String key, String value) {
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            this.header.put(key, value);
            return this;
        } else {
            return this;
        }
    }
}
