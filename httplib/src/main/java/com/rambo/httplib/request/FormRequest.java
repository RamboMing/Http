package com.rambo.httplib.request;

import com.rambo.httplib.core.HttpHeaderParser;
import com.rambo.httplib.exception.HttpException;
import com.rambo.httplib.response.NetworkResponse;
import com.rambo.httplib.response.Response;
import com.rambo.httplib.response.URLHttpResponse;
import com.rambo.httplib.utils.ByteArrayPool;
import com.rambo.httplib.utils.IOUtils;
import com.rambo.httplib.utils.JsonUtil;
import com.rambo.httplib.utils.PoolingByteArrayOutputStream;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class FormRequest extends Request {
    public FormRequest(FormRequestBuilder builder) {
        super(builder);
    }

    public String getCacheKey() {
        return this.getMethod() == 1?this.getUrl() + this.mParams.getUrlParams():this.getUrl();
    }

    public Map<String, String> getHeaders() {
        return this.header;
    }

    public RequestBody getBody() {
        RequestBody requestBody = this.gtRequestBody();
        if(this.mListener == null) {
            return requestBody;
        } else {
            CountingRequestBody countingRequestBody = new CountingRequestBody(requestBody, new CountingRequestBody.Listener() {
                public void onRequestProgress(long bytesWritten, long contentLength) {
                    FormRequest.this.mRequestQueue.getDelivery().postProgress(FormRequest.this.mListener, bytesWritten, contentLength);
                }
            });
            return countingRequestBody;
        }
    }

    private RequestBody gtRequestBody() {
        List fileParams = this.mParams.getFileParams();
        if(fileParams.isEmpty()) {
            FormBody.Builder builder1 = new FormBody.Builder();
            this.addParams(builder1);
            return builder1.build();
        } else {
            okhttp3.MultipartBody.Builder builder = (new okhttp3.MultipartBody.Builder()).setType(MultipartBody.FORM);
            this.addParams(builder);
            Iterator var3 = fileParams.iterator();

            while(var3.hasNext()) {
                FormFile formFile = (FormFile)var3.next();
                RequestBody fileBody = RequestBody.create(MediaType.parse(this.guessMimeType(formFile.getFileName())), formFile.getFile());
                builder.addFormDataPart(formFile.getKey(), formFile.getFileName(), fileBody);
            }

            return builder.build();
        }
    }

    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if(contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }

        return contentTypeFor;
    }

    private void addParams(okhttp3.MultipartBody.Builder builder, Map<String, String> params) {
        if(params != null && !params.isEmpty()) {
            Iterator var3 = params.keySet().iterator();

            while(var3.hasNext()) {
                String key = (String)var3.next();
                builder.addPart(Headers.of(new String[]{"Content-Disposition", "form-data; name=\"" + key + "\""}), RequestBody.create((MediaType)null, (String)params.get(key)));
            }
        }

    }

    public byte[] entityToBytes(URLHttpResponse httpResponse) throws IOException, HttpException {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(ByteArrayPool.get(), (int)httpResponse.getContentLength());
        byte[] buffer = null;

        try {
            InputStream in = httpResponse.getContentStream();
            if(in == null) {
                throw new HttpException(1104);
            } else {
                buffer = ByteArrayPool.get().getBuf(1024);

                int count;
                while((count = in.read(buffer)) != -1) {
                    bytes.write(buffer, 0, count);
                }

                byte[] var6 = bytes.toByteArray();
                return var6;
            }
        } finally {
            IOUtils.closeIO(new Closeable[]{httpResponse.getContentStream()});
            ByteArrayPool.get().returnBuf(buffer);
            IOUtils.closeIO(new Closeable[]{bytes});
        }
    }

    public Priority getPriority() {
        return Priority.IMMEDIATE;
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

        return Response.success(response.data, response.headers, HttpHeaderParser.parseCacheHeaders(this, response));
    }
}
