package com.rambo.httplib.core;

import com.rambo.httplib.exception.HttpException;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */

public  interface RetryPolicy {
    int getCurrentTimeout();

    int getCurrentRetryCount();

    void retry(HttpException var1) throws HttpException;
}
