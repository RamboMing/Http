package com.rambo.httplib.response;

import com.rambo.httplib.cache.Cache;
import com.rambo.httplib.exception.HttpException;

import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class Response<T> {
    public final T result;
    public final Cache.Entry cacheEntry;
    public final HttpException error;
    public boolean intermediate = false;
    public final Map<String, String> headers;

    public static <T> Response<T> success(T result, Map<String, String> headers, Cache.Entry cacheEntry) {
        return new Response(result, headers, cacheEntry);
    }

    public static <T> Response<T> error(HttpException error) {
        return new Response(error);
    }

    public boolean isSuccess() {
        return this.error == null;
    }

    private Response(T result, Map<String, String> headers, Cache.Entry cacheEntry) {
        this.result = result;
        this.cacheEntry = cacheEntry;
        this.headers = headers;
        this.error = null;
    }

    private Response(HttpException error) {
        this.result = null;
        this.cacheEntry = null;
        this.error = error;
        this.headers = null;
    }

    public interface ErrorListener {
        void onErrorResponse(HttpException var1);
    }

    public interface Listener<T> {
        void onResponse(T var1);
    }
}

