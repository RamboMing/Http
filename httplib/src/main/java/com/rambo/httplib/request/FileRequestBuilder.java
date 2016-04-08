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
public class FileRequestBuilder extends RequestBuilder {
    public FileRequestBuilder() {
    }

    public FileRequestBuilder method(int method) {
        this.method = method;
        return this;
    }

    public FileRequestBuilder url(String url) {
        if(TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url == null");
        } else {
            this.url = url;
            return this;
        }
    }

    public FileRequestBuilder redirectUrl(String redirectUrl) {
        if(TextUtils.isEmpty(redirectUrl)) {
            throw new IllegalArgumentException("redirectUrl == null");
        } else {
            this.redirectUrl = redirectUrl;
            return this;
        }
    }

    public FileRequestBuilder shouldCache(boolean shouldCache) {
        this.shouldCache = shouldCache;
        return this;
    }

    public FileRequestBuilder shouldFromCache(boolean shouldFromCache) {
        this.shouldFromCache = shouldFromCache;
        return this;
    }

    public FileRequestBuilder shouldOfflineCache(boolean shouldOfflineCache) {
        this.shouldOfflineCache = shouldOfflineCache;
        return this;
    }

    public FileRequestBuilder forceCache(boolean forceCache) {
        this.forceCache = forceCache;
        return this;
    }

    public FileRequestBuilder maxCacheAage(int maxCacheAage) {
        if(maxCacheAage < 0) {
            maxCacheAage = 0;
        }

        this.maxCacheAage = maxCacheAage;
        return this;
    }

    public FileRequestBuilder retryPolicy(RetryPolicy retryPolicy) {
        if(retryPolicy == null) {
            return this;
        } else {
            this.retryPolicy = retryPolicy;
            return this;
        }
    }

    public FileRequestBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    public FileRequestBuilder params(HttpParams params) {
        if(params == null) {
            return this;
        } else {
            this.params = params;
            return this;
        }
    }

    public FileRequestBuilder addUrlParams(String key, String value) {
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            this.params.getUrlParams().put(key, value);
            return this;
        } else {
            return this;
        }
    }

    public FileRequestBuilder addFileParams(String key, String value) {
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            FormFile file = new FormFile(key, value);
            this.params.getFileParams().add(file);
            return this;
        } else {
            return this;
        }
    }

    public FileRequestBuilder trafficStatsTag(int trafficStatsTag) {
        this.trafficStatsTag = trafficStatsTag;
        return this;
    }

    public FileRequestBuilder rspClass(Class rspClass) {
        this.rspClass = rspClass;
        return this;
    }

    public FileRequestBuilder writeTimeoutMs(int writeTimeoutMs) {
        this.writeTimeoutMs = writeTimeoutMs;
        return this;
    }

    public FileRequestBuilder readTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
        return this;
    }

    public FileRequestBuilder connTimeoutMs(int connTimeoutMs) {
        this.connTimeoutMs = connTimeoutMs;
        return this;
    }

    public FileRequestBuilder storePath(String storePath) {
        this.storePath = storePath;
        return this;
    }

    public FileRequest build() {
        return new FileRequest(this);
    }

    public static FileRequestBuilder create() {
        return new FileRequestBuilder();
    }

    public FileRequestBuilder cookieJar(CookieJar cookieJar) {
        if(cookieJar == null) {
            cookieJar = CookieJar.NO_COOKIES;
        }

        this.cookieJar = cookieJar;
        return this;
    }

    public FileRequestBuilder header(Map<String, String> headers) {
        if(headers != null && !headers.isEmpty()) {
            this.header = headers;
            return this;
        } else {
            return this;
        }
    }

    public FileRequestBuilder addHeader(String key, String value) {
        if(!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            this.header.put(key, value);
            return this;
        } else {
            return this;
        }
    }
}
