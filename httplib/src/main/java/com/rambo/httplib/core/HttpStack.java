package com.rambo.httplib.core;

import com.rambo.httplib.exception.HttpException;
import com.rambo.httplib.request.Request;
import com.rambo.httplib.response.URLHttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public interface HttpStack {
    URLHttpResponse performRequest(Request<?> var1, Map<String, String> var2) throws IOException, HttpException;
}
