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
public class FormRequestBuilder extends RequestBuilder {
    public FormRequestBuilder() {
    }

    public FormRequestBuilder method(int method) {
        this.method = method;
        return this;
    }

    public FormRequestBuilder url(String url) {
        if(TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url == null");
        } else {
            this.url = url;
            return this;
        }
    }

    public FormRequestBuilder redirectUrl(String redirectUrl) {
        if(TextUtils.isEmpty(redirectUrl)) {
            throw new IllegalArgumentException("redirectUrl == null");
        } else {
            this.redirectUrl = redirectUrl;
            return this;
        }
    }

    public FormRequestBuilder shouldCache(boolean shouldCache) {
        this.shouldCache = shouldCache;
        return this;
    }

    public FormRequestBuilder shouldFromCache(boolean shouldFromCache) {
        this.shouldFromCache = shouldFromCache;
        return this;
    }

    public FormRequestBuilder shouldOfflineCache(boolean shouldOfflineCache) {
        this.shouldOfflineCache = shouldOfflineCache;
        return this;
    }

    public FormRequestBuilder forceCache(boolean forceCache) {
        this.forceCache = forceCache;
        return this;
    }

    public FormRequestBuilder maxCacheAage(int maxCacheAage) {
        if(maxCacheAage < 0) {
            maxCacheAage = 0;
        }

        this.maxCacheAage = maxCacheAage;
        return this;
    }

    public FormRequestBuilder retryPolicy(RetryPolicy retryPolicy) {
        if(retryPolicy == null) {
            return this;
        } else {
            this.retryPolicy = retryPolicy;
            return this;
        }
    }

    public FormRequestBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    public FormRequestBuilder params(HttpParams params) {
        if(params == null) {
            return this;
        } else {
            this.params = params;
            return this;
        }
    }

    public FormRequestBuilder addUrlParams(String key, String value) {
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            this.params.getUrlParams().put(key, value);
            return this;
        } else {
            return this;
        }
    }

    public FormRequestBuilder addFileParams(String key, String value) {
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            FormFile file = new FormFile(key, value);
            this.params.getFileParams().add(file);
            return this;
        } else {
            return this;
        }
    }

    public FormRequestBuilder trafficStatsTag(int trafficStatsTag) {
        this.trafficStatsTag = trafficStatsTag;
        return this;
    }

    public FormRequestBuilder rspClass(Class rspClass) {
        this.rspClass = rspClass;
        return this;
    }

    public FormRequestBuilder writeTimeoutMs(int writeTimeoutMs) {
        this.writeTimeoutMs = writeTimeoutMs;
        return this;
    }

    public FormRequestBuilder readTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
        return this;
    }

    public FormRequestBuilder connTimeoutMs(int connTimeoutMs) {
        this.connTimeoutMs = connTimeoutMs;
        return this;
    }

    public FormRequestBuilder storePath(String storePath) {
        return this;
    }

    public FormRequest build() {
        return new FormRequest(this);
    }

    public static FormRequestBuilder create() {
        return new FormRequestBuilder();
    }

    public FormRequestBuilder cookieJar(CookieJar cookieJar) {
        if(cookieJar == null) {
            cookieJar = CookieJar.NO_COOKIES;
        }

        this.cookieJar = cookieJar;
        return this;
    }

    public FormRequestBuilder header(Map<String, String> headers) {
        if(headers != null && !headers.isEmpty()) {
            this.header = headers;
            return this;
        } else {
            return this;
        }
    }

    public FormRequestBuilder addHeader(String key, String value) {
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            this.header.put(key, value);
            return this;
        } else {
            return this;
        }
    }
}
