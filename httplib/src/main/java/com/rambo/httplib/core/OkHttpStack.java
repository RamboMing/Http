package com.rambo.httplib.core;

import com.rambo.httplib.request.Request;
import com.rambo.httplib.response.URLHttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class OkHttpStack implements HttpStack {
    public OkHttpStack() {
    }

    public URLHttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        int timeoutMs = request.getTimeoutMs();
        int readTimeoutMs = request.getReadTimeoutMs();
        int writeTimeoutMs = request.getWriteTimeoutMs();
        builder.connectTimeout((long)timeoutMs, TimeUnit.MILLISECONDS);
        builder.readTimeout((long)readTimeoutMs, TimeUnit.MILLISECONDS);
        builder.writeTimeout((long)writeTimeoutMs, TimeUnit.MILLISECONDS);
        builder.cookieJar(request.getCookieJar());
        okhttp3.Request.Builder okHttpRequestBuilder = new okhttp3.Request.Builder();
        String url = request.getMethod() == 0?request.getUrl() + request.getHttpParams().getUrlParamsStr():request.getUrl();
        okHttpRequestBuilder.url(url);
        HashMap map = new HashMap();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        Iterator okHttpRequest = map.keySet().iterator();

        while(okHttpRequest.hasNext()) {
            String okHttpCall = (String)okHttpRequest.next();
            okHttpRequestBuilder.addHeader(okHttpCall, (String)map.get(okHttpCall));
        }

        setConnectionParametersForRequest(okHttpRequestBuilder, request);
        okhttp3.Request okHttpRequest1 = okHttpRequestBuilder.build();
        Call okHttpCall1 = builder.build().newCall(okHttpRequest1);
        Response okHttpResponse = okHttpCall1.execute();
        return this.responseFromConnection(okHttpResponse);
    }

    private URLHttpResponse responseFromConnection(Response okHttpResponse) throws IOException {
        URLHttpResponse response = new URLHttpResponse();
        int responseCode = okHttpResponse.code();
        if(responseCode == -1) {
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        } else {
            response.setResponseCode(responseCode);
            response.setResponseMessage(okHttpResponse.message());
            response.setContentStream(okHttpResponse.body().byteStream());
            response.setContentLength(okHttpResponse.body().contentLength());
            response.setContentEncoding(okHttpResponse.header("Content-Encoding"));
            if(okHttpResponse.body().contentType() != null) {
                response.setContentType(okHttpResponse.body().contentType().type());
            }

            HashMap headerMap = new HashMap();
            Headers responseHeaders = okHttpResponse.headers();
            int i = 0;

            for(int len = responseHeaders.size(); i < len; ++i) {
                String name = responseHeaders.name(i);
                String value = responseHeaders.value(i);
                if(name != null) {
                    headerMap.put(name, value);
                }
            }

            response.setHeaders(headerMap);
            return response;
        }
    }

    private static void setConnectionParametersForRequest(okhttp3.Request.Builder builder, Request<?> request) throws IOException {
        switch(request.getMethod()) {
            case 0:
                builder.get();
                break;
            case 1:
                builder.post(request.getBody());
                break;
            case 2:
                builder.put(request.getBody());
                break;
            case 3:
                builder.delete();
                break;
            case 4:
                builder.head();
                break;
            case 5:
                builder.method("OPTIONS", (RequestBody)null);
                break;
            case 6:
                builder.method("TRACE", (RequestBody)null);
                break;
            case 7:
                builder.patch(request.getBody());
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }

    }
}
