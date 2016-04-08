package com.rambo.httplib.request;

import com.rambo.httplib.core.HttpHeaderParser;
import com.rambo.httplib.response.NetworkResponse;
import com.rambo.httplib.response.Response;
import com.rambo.httplib.utils.JsonUtil;

import java.io.UnsupportedEncodingException;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class GetRequest extends Request {
    public GetRequest(GetRequestBuilder builder) {
        super(builder);
    }

    public RequestBody getBody() {
        FormBody.Builder builder = new FormBody.Builder();
        this.addParams(builder);
        return builder.build();
    }

    public Response parseNetworkResponse(NetworkResponse response) {
        try {
            if(this.rspClazz != null) {
                String e = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                Object t = JsonUtil.fromJson(e, this.rspClazz);
                return Response.success(t, response.headers, HttpHeaderParser.parseCacheHeaders(this, response));
            }
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
        }

        return Response.success(new String(response.data), response.headers, HttpHeaderParser.parseCacheHeaders(this, response));
    }
}
