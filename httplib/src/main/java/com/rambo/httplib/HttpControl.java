package com.rambo.httplib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.rambo.httplib.cache.Cache;
import com.rambo.httplib.cache.DiskBasedCache;
import com.rambo.httplib.cache.NoNetWorkCache;
import com.rambo.httplib.cookie.OkSessionCookiejar;
import com.rambo.httplib.core.BasicNetwork;
import com.rambo.httplib.core.HttpStack;
import com.rambo.httplib.core.OkHttpStack;
import com.rambo.httplib.core.RequestQueue;
import com.rambo.httplib.exception.HttpException;
import com.rambo.httplib.request.FileRequest;
import com.rambo.httplib.request.FileRequestBuilder;
import com.rambo.httplib.request.FormRequest;
import com.rambo.httplib.request.FormRequestBuilder;
import com.rambo.httplib.request.GetRequest;
import com.rambo.httplib.request.GetRequestBuilder;
import com.rambo.httplib.request.HttpParams;
import com.rambo.httplib.request.Request;
import com.rambo.httplib.request.RequestBuilder;
import com.rambo.httplib.response.HttpResponseListnener;
import com.rambo.httplib.utils.NetTool;

import java.io.File;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.CookieJar;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class HttpControl {
    private static final String DEFAULT_CACHE_NAME = "httpcache";
    private static final String DEFAULT_COOKIE_NAME = "httpcookie";
    private static final int DEFAULT_MAX_AGE = 10;
    private static final int CREATE_CLIENT_ERROR = 1001;
    private static final int NOT_WORK_CODE = 1002;
    private static HttpControl instance;
    private RequestQueue requestQueue;
    private Context mContext;
    private Object requestTag;
    private String cachePath;
    private String cacheFileName;
    private String cookiePath;
    private String cookieFileName;

    public static HttpControl getInstance(Context context, String cachePath, String cacheFileName, String cookiePath, String cookieFileName) {
        if(instance == null) {
            Class var5 = HttpControl.class;
            synchronized(HttpControl.class) {
                if(instance == null) {
                    instance = new HttpControl(context, TextUtils.isEmpty(cachePath)?context.getCacheDir().getAbsolutePath():cachePath, TextUtils.isEmpty(cacheFileName)?"httpcache":cacheFileName, TextUtils.isEmpty(cookiePath)?context.getCacheDir().getAbsolutePath():cookiePath, TextUtils.isEmpty(cookieFileName)?"httpcookie":cookieFileName);
                }
            }
        }

        return instance;
    }

    public static HttpControl getInstance(Context context, Cache cache, String cookiePath, String cookieFileName) {
        if(instance == null) {
            Class var4 = HttpControl.class;
            synchronized(HttpControl.class) {
                if(instance == null) {
                    instance = new HttpControl(context, cache, TextUtils.isEmpty(cookiePath)?context.getCacheDir().getAbsolutePath():cookiePath, TextUtils.isEmpty(cookieFileName)?"httpcookie":cookieFileName);
                }
            }
        }

        return instance;
    }

    public static HttpControl getInstance(Context context, String cachePath, String cookiePathPath) {
        return getInstance(context, TextUtils.isEmpty(cachePath)?context.getCacheDir().getAbsolutePath():cachePath, "httpcache", TextUtils.isEmpty(cookiePathPath)?context.getCacheDir().getAbsolutePath():cookiePathPath, "httpcookie");
    }

    public static HttpControl getInstance(Context context) {
        return getInstance(context, context.getCacheDir().getAbsolutePath(), "httpcache", context.getCacheDir().getAbsolutePath(), "httpcookie");
    }

    public static HttpControl getInstance(Context context, Cache cache) {
        return getInstance(context, cache, context.getCacheDir().getAbsolutePath(), "httpcookie");
    }

    private HttpControl(Context context, String cachePath, String cacheFillName, String cookieCahePath, String cookieFileName) {
        this.mContext = context;
        this.cachePath = cachePath;
        this.cacheFileName = cacheFillName;
        this.requestQueue = newRequestQueue(context, cachePath, cacheFillName, cookieCahePath, cookieFileName);
        this.requestQueue.start();
        this.cookiePath = cookieCahePath;
        this.cookieFileName = cookieFileName;
    }

    private HttpControl(Context context, Cache cache, String cookieCahePath, String cookieFileName) {
        this.mContext = context;
        this.requestQueue = newRequestQueue(context, cache, cookieCahePath, cookieFileName);
        this.cookiePath = cookieCahePath;
        this.cookieFileName = cookieFileName;
    }

    public <T> void getJsonObj(int method, String url, Class<T> clazz) {
        this.getJsonObj(method, url, (Map)null, this.requestTag, 0, true, true, false, (HttpResponseListnener)null, clazz);
    }

    public <T> void getJsonObj(int method, String url, Map<String, String> urlParams, Class<T> clazz) {
        this.getJsonObj(method, url, urlParams, this.requestTag, 0, true, true, false, (HttpResponseListnener)null, clazz);
    }

    public <T> void getJsonObj(int method, String url, HttpResponseListnener listener, Class<T> clazz) {
        this.getJsonObj(method, url, (Map)null, this.requestTag, 0, true, true, false, listener, clazz);
    }

    public <T> void getJsonObj(int method, String url, Map<String, String> urlParams, HttpResponseListnener listener, Class<T> clazz) {
        this.getJsonObj(method, url, urlParams, this.requestTag, 0, true, true, false, listener, clazz);
    }

    public <T> void getJsonObjNoCache(int method, String url, Class<T> clazz) {
        this.getJsonObj(method, url, (Map)null, this.requestTag, 0, false, false, false, (HttpResponseListnener)null, clazz);
    }

    public <T> void getJsonObjNoCache(int method, String url, Map<String, String> urlParams, Class<T> clazz) {
        this.getJsonObj(method, url, urlParams, this.requestTag, 0, false, false, false, (HttpResponseListnener)null, clazz);
    }

    public <T> void getJsonObjNoCache(int method, String url, HttpResponseListnener listener, Class<T> clazz) {
        this.getJsonObj(method, url, (Map)null, this.requestTag, 0, false, false, false, listener, clazz);
    }

    public <T> void getJsonObjNoCache(int method, String url, Map<String, String> urlParams, HttpResponseListnener listener, Class<T> clazz) {
        this.getJsonObj(method, url, urlParams, this.requestTag, 0, false, false, false, listener, clazz);
    }

    public <T> void getJsonObjForceCache(int method, String url, Class<T> clazz) {
        this.getJsonObj(method, url, (Map)null, this.requestTag, 10, true, true, true, (HttpResponseListnener)null, clazz);
    }

    public <T> void getJsonObjForceCache(int method, String url, Map<String, String> urlParams, Class<T> clazz) {
        this.getJsonObj(method, url, urlParams, this.requestTag, 10, true, true, true, (HttpResponseListnener)null, clazz);
    }

    public <T> void getJsonObjForceCache(int method, String url, HttpResponseListnener listener, Class<T> clazz) {
        this.getJsonObj(method, url, (Map)null, this.requestTag, 10, true, true, true, listener, clazz);
    }

    public <T> void getJsonObjForceCache(int method, String url, Map<String, String> urlParams, HttpResponseListnener listener, Class<T> clazz) {
        this.getJsonObj(method, url, urlParams, this.requestTag, 10, true, true, true, listener, clazz);
    }

    public <T> void getJsonObjForceCache(int method, String url, int maxage, Class<T> clazz) {
        this.getJsonObj(method, url, (Map)null, this.requestTag, maxage, true, true, true, (HttpResponseListnener)null, clazz);
    }

    public <T> void getJsonObjForceCache(int method, String url, Map<String, String> urlParams, int maxage, Class<T> clazz) {
        this.getJsonObj(method, url, urlParams, this.requestTag, maxage, true, true, true, (HttpResponseListnener)null, clazz);
    }

    public <T> void getJsonObjForceCache(int method, String url, int maxage, HttpResponseListnener listener, Class<T> clazz) {
        this.getJsonObj(method, url, (Map)null, this.requestTag, maxage, true, true, true, listener, clazz);
    }

    public <T> void getJsonObjForceCache(int method, String url, Map<String, String> urlParams, int maxage, HttpResponseListnener listener, Class<T> clazz) {
        this.getJsonObj(method, url, urlParams, this.requestTag, maxage, true, true, true, listener, clazz);
    }

    protected <T> void getJsonObj(int method, String url, Map<String, String> urlParams, Object tag, int maxAge, boolean shoudCache, boolean isFromCache, boolean isForceCache, final HttpResponseListnener listener, Class<T> clazz) {
        HttpParams httpParams = new HttpParams();
        httpParams.put(urlParams);
        GetRequestBuilder builder = GetRequestBuilder.create().method(method).url(url).tag(tag).maxCacheAage(maxAge).shouldCache(shoudCache).shouldFromCache(isFromCache).forceCache(isForceCache).params(httpParams).rspClass(clazz).cookieJar(this.getCookieJar());
        GetRequest request = builder.build();
        request.setListener(listener);
        if(NetTool.isNetworkConnected(this.mContext)) {
            this.requestQueue.add(request);
        } else {
            Cache cache = this.requestQueue.getCache();
            if((cache != null || cache instanceof NoNetWorkCache) && request.isForcache()) {
                NoNetWorkCache noNetWorkCache = (NoNetWorkCache)cache;
                noNetWorkCache.cache(request, listener, new HttpException(1103));
                return;
            }

            (new Handler(Looper.getMainLooper())).post(new Runnable() {
                public void run() {
                    listener.onFailure(new HttpException(1103));
                }
            });
        }

    }

    public void getStr(int method, String url) {
        this.getJsonStr(method, url, (Map)null, this.requestTag, 0, true, true, false, (HttpResponseListnener)null);
    }

    public void getStr(int method, String url, HttpResponseListnener listener) {
        this.getJsonStr(method, url, (Map)null, this.requestTag, 0, true, true, false, listener);
    }

    public void getStr(int method, String url, Map<String, String> urlParams, HttpResponseListnener listener) {
        this.getJsonStr(method, url, urlParams, this.requestTag, 0, true, true, false, listener);
    }

    public void getStr(int method, String url, String tag, HttpResponseListnener listener) {
        this.getJsonStr(method, url, (Map)null, tag, 0, true, true, false, listener);
    }

    public void getStrNoCache(int method, String url) {
        this.getJsonStr(method, url, (Map)null, this.requestTag, 0, false, false, false, (HttpResponseListnener)null);
    }

    public void getStrNoCache(int method, String url, Map<String, String> urlParams) {
        this.getJsonStr(method, url, urlParams, this.requestTag, 0, false, false, false, (HttpResponseListnener)null);
    }

    public void getStrNoCache(int method, String url, HttpResponseListnener listener) {
        this.getJsonStr(method, url, (Map)null, this.requestTag, 0, false, false, false, listener);
    }

    public void getStrNoCache(int method, String url, Map<String, String> urlParams, HttpResponseListnener listener) {
        this.getJsonStr(method, url, urlParams, this.requestTag, 0, false, false, false, listener);
    }

    public void getStrForceCache(int method, String url, int maxage) {
        this.getJsonStr(method, url, (Map)null, this.requestTag, maxage, true, true, true, (HttpResponseListnener)null);
    }

    public void getStrForceCache(int method, String url, Map<String, String> urlParams, int maxage) {
        this.getJsonStr(method, url, urlParams, this.requestTag, maxage, true, true, true, (HttpResponseListnener)null);
    }

    public void getStrForceCache(int method, String url, int maxage, HttpResponseListnener listener) {
        this.getJsonStr(method, url, (Map)null, this.requestTag, maxage, true, true, true, listener);
    }

    public void getStrForceCache(int method, String url, Map<String, String> urlParams, int maxage, HttpResponseListnener listener) {
        this.getJsonStr(method, url, urlParams, this.requestTag, maxage, true, true, true, listener);
    }

    public void getStrForceCache(int method, String url) {
        this.getJsonStr(method, url, (Map)null, this.requestTag, 10, true, true, true, (HttpResponseListnener)null);
    }

    public void getStrForceCache(int method, String url, Map<String, String> urlParams) {
        this.getJsonStr(method, url, urlParams, this.requestTag, 10, true, true, true, (HttpResponseListnener)null);
    }

    public void getStrForceCache(int method, String url, HttpResponseListnener listener) {
        this.getJsonStr(method, url, (Map)null, this.requestTag, 10, true, true, true, listener);
    }

    public void getStrForceCache(int method, String url, Map<String, String> urlParams, HttpResponseListnener listener) {
        this.getJsonStr(method, url, urlParams, this.requestTag, 10, true, true, true, listener);
    }

    protected void getJsonStr(int method, String url, Map<String, String> urlParams, Object tag, int maxAge, boolean shoudCache, boolean isFromCache, boolean isForceCache, final HttpResponseListnener listener) {
        HttpParams httpParams = new HttpParams();
        httpParams.put(urlParams);
        GetRequestBuilder builder = GetRequestBuilder.create().method(method).url(url).tag(tag).maxCacheAage(maxAge).shouldCache(shoudCache).shouldFromCache(isFromCache).forceCache(isForceCache).params(httpParams).cookieJar(this.getCookieJar());
        GetRequest request = builder.build();
        request.setListener(listener);
        if(NetTool.isNetworkConnected(this.mContext)) {
            this.requestQueue.add(request);
        } else {
            Cache cache = this.requestQueue.getCache();
            if((cache != null || cache instanceof NoNetWorkCache) && request.isForcache()) {
                NoNetWorkCache noNetWorkCache = (NoNetWorkCache)cache;
                noNetWorkCache.cache(request, listener, new HttpException(1103));
                return;
            }

            (new Handler(Looper.getMainLooper())).post(new Runnable() {
                public void run() {
                    listener.onFailure(new HttpException(1103));
                }
            });
        }

    }

    public void download(String url, String fileAbsPath, HttpResponseListnener responseHandler) {
        this.download(url, fileAbsPath, (Map)null, responseHandler);
    }

    public void download(String url, String fileAbsPath, Map<String, String> urlParams, final HttpResponseListnener responseHandler) {
        if(NetTool.isNetworkConnected(this.mContext)) {
            HttpParams params = new HttpParams();
            params.put(urlParams);
            FileRequestBuilder builder = FileRequestBuilder.create().method(0).url(url).storePath(fileAbsPath).params(params).readTimeoutMs('\uea60').readTimeoutMs('\uea60').cookieJar(this.getCookieJar());
            FileRequest request = builder.build();
            request.setListener(responseHandler);
            this.requestQueue.add(request);
        } else {
            (new Handler(Looper.getMainLooper())).post(new Runnable() {
                public void run() {
                    responseHandler.onFailure(new HttpException(1103));
                }
            });
        }

    }

    public void upload(String url, String filePath, HttpResponseListnener responseHandler, Class clazz) {
        HttpParams params = new HttpParams();
        params.addFile(filePath);
        this.upload(url, params, responseHandler, clazz);
    }

    public void upload(String url, String filePath, HttpResponseListnener responseHandler) {
        this.upload(url, (String)filePath, responseHandler, (Class)null);
    }

    public void upload(String url, String filePath) {
        this.upload(url, (String)filePath, (HttpResponseListnener)null, (Class)null);
    }

    public void upload(String url, List<String> filePaths, HttpResponseListnener responseHandler, Class clazz) {
        HttpParams params = new HttpParams();
        params.addFiles(filePaths);
        this.upload(url, params, responseHandler, clazz);
    }

    public void upload(String url, List<String> filePaths, HttpResponseListnener responseHandler) {
        this.upload(url, (List)filePaths, responseHandler, (Class)null);
    }

    public void upload(String url, List<String> filePaths) {
        this.upload(url, (List)filePaths, (HttpResponseListnener)null, (Class)null);
    }

    public void upload(String url, HttpParams params) {
        this.upload(url, (HttpParams)params, (HttpResponseListnener)null, (Class)null);
    }

    public void upload(String url, HttpParams params, HttpResponseListnener responseHandler) {
        this.upload(url, (HttpParams)params, responseHandler, (Class)null);
    }

    public <T> void createRequest(RequestBuilder builder, final HttpResponseListnener<T> listener) {
        if(builder == null) {
            throw new IllegalArgumentException("build not null");
        } else {
            Request request = builder.build();
            if(NetTool.isNetworkConnected(this.mContext)) {
                request.setListener(listener);
                this.requestQueue.add(request);
            } else {
                Cache cache = this.requestQueue.getCache();
                if((cache != null || cache instanceof NoNetWorkCache) && request.isForcache()) {
                    NoNetWorkCache noNetWorkCache = (NoNetWorkCache)cache;
                    noNetWorkCache.cache(request, listener, new HttpException(1103));
                    return;
                }

                (new Handler(Looper.getMainLooper())).post(new Runnable() {
                    public void run() {
                        listener.onFailure(new HttpException(1103));
                    }
                });
            }

        }
    }

    public void upload(String url, HttpParams params, final HttpResponseListnener responseHandler, Class clazz) {
        if(NetTool.isNetworkConnected(this.mContext)) {
            FormRequestBuilder builder = FormRequestBuilder.create().method(1).url(url).params(params).rspClass(clazz).connTimeoutMs(30000).cookieJar(this.getCookieJar());
            FormRequest request = builder.build();
            request.setListener(responseHandler);
            this.requestQueue.add(request);
        } else {
            (new Handler(Looper.getMainLooper())).post(new Runnable() {
                public void run() {
                    responseHandler.onFailure(new HttpException(1103));
                }
            });
        }

    }

    public Map<String, String> getCookies() {
        File cookieCacheDir = new File(this.cookiePath, this.cookieFileName);
        DiskBasedCache cache = new DiskBasedCache(cookieCacheDir);
        cache.initialize();
        Cache.Entry cookie = cache.get("token");
        return cookie != null?cookie.responseHeaders:null;
    }

    private String getParams(Map<String, String> urlParams) {
        StringBuilder result = new StringBuilder();

        try {
            Iterator var3 = urlParams.entrySet().iterator();

            while(var3.hasNext()) {
                java.util.Map.Entry entry = (java.util.Map.Entry)var3.next();
                if(result.length() > 0) {
                    result.append('&');
                }

                result.append(URLEncoder.encode((String) entry.getKey(), "utf-8"));
                result.append('=');
                result.append(URLEncoder.encode((String)entry.getValue(), "utf-8"));
            }
        } catch (Exception var5) {
            ;
        }

        return result.toString();
    }

    private Cache getCookieCache() {
        File cookieCacheDir = new File(this.cookiePath, this.cookieFileName);
        DiskBasedCache cache = new DiskBasedCache(cookieCacheDir);
        return cache;
    }

    public static RequestQueue newRequestQueue(Context context, String cachePath, String cacheFileName, String cookiePath, String cookieFileName, HttpStack stack) {
        RequestQueue queue = null;

        try {
            File e = new File(cachePath);
            if(!e.exists()) {
                e.mkdirs();
            }

            File cacheDir = new File(e, cacheFileName);
            String userAgent = "volley/0";
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            (new StringBuilder()).append(packageName).append("/").append(info.versionCode).toString();
            if(stack == null) {
                stack = new OkHttpStack();
            }

            BasicNetwork network = new BasicNetwork((HttpStack)stack);
            queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
            queue.start();
        } catch (Exception var13) {
            var13.printStackTrace();
        }

        return queue;
    }

    public static RequestQueue newRequestQueue(Context context, Cache cache, String cookiePath, String cookieFileName, HttpStack stack) {
        RequestQueue queue = null;

        try {
            String e = "volley/0";
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            (new StringBuilder()).append(packageName).append("/").append(info.versionCode).toString();
            if(stack == null) {
                stack = new OkHttpStack();
            }

            BasicNetwork network = new BasicNetwork((HttpStack)stack);
            queue = new RequestQueue(cache, network);
            queue.start();
        } catch (Exception var10) {
            var10.printStackTrace();
        }

        return queue;
    }

    public static RequestQueue newRequestQueue(Context context, String cachePath, String cacheFileName, String cookiePath, String cookieFileName) {
        return newRequestQueue(context, cachePath, cacheFileName, cookiePath, cookieFileName, (HttpStack)null);
    }

    public static RequestQueue newRequestQueue(Context context, Cache cache, String cookiePath, String cookieFileName) {
        return newRequestQueue(context, (Cache)cache, cookiePath, cookieFileName, (HttpStack)null);
    }

    public CookieJar getCookieJar() {
        if(!TextUtils.isEmpty(this.cookiePath) && !TextUtils.isEmpty(this.cookieFileName)) {
            File cookieFile = new File(this.cookiePath, this.cookieFileName);
            DiskBasedCache diskBasedCache = new DiskBasedCache(cookieFile);
            return new OkSessionCookiejar(diskBasedCache);
        } else {
            return null;
        }
    }

    public <T> void cancle(Request<T> request) {
        this.requestQueue.finish(request);
    }

    public <T> void cancle(Object tag) {
        this.requestQueue.cancelAll(tag);
    }

    public <T> void cancle(RequestQueue.RequestFilter filter) {
        this.requestQueue.cancelAll(filter);
    }

    public Object getTag() {
        return this.requestTag;
    }

    public void setTag(Object tag) {
        this.requestTag = tag;
    }

    public interface Method {
        int DEPRECATED_GET_OR_POST = -1;
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }
}
