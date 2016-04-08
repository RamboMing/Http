package com.rambo.httplib.response;

import com.rambo.httplib.exception.HttpException;
import com.rambo.httplib.request.Request;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public interface ResponseDelivery {
    void postResponse(boolean var1, Request<?> var2, Response<?> var3);

    void postResponse(boolean var1, Request<?> var2, Response<?> var3, Runnable var4);

    void postError(boolean var1, Request<?> var2, HttpException var3);

    void postProgress(HttpResponseListnener var1, long var2, long var4);
}
