package com.rambo.httplib.core;

import com.rambo.httplib.exception.HttpException;
import com.rambo.httplib.request.Request;
import com.rambo.httplib.response.NetworkResponse;


/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public interface Network {
    NetworkResponse performRequest(Request<?> var1) throws HttpException;
}