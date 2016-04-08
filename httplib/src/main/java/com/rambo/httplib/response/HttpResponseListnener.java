package com.rambo.httplib.response;

import com.rambo.httplib.exception.HttpException;

import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public abstract class HttpResponseListnener<T> {
    public HttpResponseListnener() {
    }

    public void onStart() {
    }

    public abstract void onSuccess(boolean var1, Map<String, String> var2, T var3);

    public void onFailure(HttpException exception) {
    }

    public void onProgress(long currentSize, long totalSize, float porgress) {
    }
}
