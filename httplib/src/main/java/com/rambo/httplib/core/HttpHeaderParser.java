package com.rambo.httplib.core;

import com.rambo.httplib.cache.Cache;
import com.rambo.httplib.request.Request;
import com.rambo.httplib.response.NetworkResponse;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;

import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class HttpHeaderParser {
    public HttpHeaderParser() {
    }

    public static Cache.Entry parseCacheHeaders(Request request, NetworkResponse response) {
        long now = System.currentTimeMillis();
        Map headers = response.headers;
        long serverDate = 0L;
        long lastModified = 0L;
        long serverExpires = 0L;
        long maxAge = (long)request.getMaxCacheAage();
        long softExpire = 1000L * maxAge + now;
        long finalExpire = 1000L * maxAge + now;
        long staleWhileRevalidate = 0L;
        boolean hasCacheControl = false;
        boolean mustRevalidate = false;
        String serverEtag = null;
        String headerValue = (String)headers.get("Date");
        if(headerValue != null) {
            serverDate = parseDateAsEpoch(headerValue);
        }

        headerValue = (String)headers.get("Cache-Control");
        if(headerValue != null) {
            hasCacheControl = true;
            String[] entry = headerValue.split(",");

            for(int i = 0; i < entry.length; ++i) {
                String token = entry[i].trim();
                if(token.equals("no-cache") || token.equals("no-store")) {
                    return null;
                }

                if(token.startsWith("max-age=")) {
                    try {
                        maxAge = Long.parseLong(token.substring(8));
                    } catch (Exception var28) {
                        ;
                    }
                } else if(token.startsWith("stale-while-revalidate=")) {
                    try {
                        staleWhileRevalidate = Long.parseLong(token.substring(23));
                    } catch (Exception var27) {
                        ;
                    }
                } else if(token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                    mustRevalidate = true;
                }
            }
        }

        headerValue = (String)headers.get("Expires");
        if(headerValue != null) {
            serverExpires = parseDateAsEpoch(headerValue);
        }

        headerValue = (String)headers.get("Last-Modified");
        if(headerValue != null) {
            lastModified = parseDateAsEpoch(headerValue);
        }

        serverEtag = (String)headers.get("ETag");
        if(hasCacheControl) {
            softExpire = now + maxAge * 1000L;
            finalExpire = mustRevalidate?softExpire:softExpire + staleWhileRevalidate * 1000L;
        } else if(serverDate > 0L && serverExpires >= serverDate) {
            softExpire = now + (serverExpires - serverDate);
            finalExpire = softExpire;
        }

        Cache.Entry var29 = new Cache.Entry();
        var29.data = response.data;
        var29.etag = serverEtag;
        var29.softTtl = softExpire;
        var29.ttl = finalExpire;
        var29.serverDate = serverDate;
        var29.lastModified = lastModified;
        var29.responseHeaders = headers;
        return var29;
    }

    public static long parseDateAsEpoch(String dateStr) {
        try {
            return DateUtils.parseDate(dateStr).getTime();
        } catch (DateParseException var2) {
            return 0L;
        }
    }

    public static String parseCharset(Map<String, String> headers, String defaultCharset) {
        String contentType = (String)headers.get("Content-Type");
        if(contentType != null) {
            String[] params = contentType.split(";");

            for(int i = 1; i < params.length; ++i) {
                String[] pair = params[i].trim().split("=");
                if(pair.length == 2 && pair[0].equals("charset")) {
                    return pair[1];
                }
            }
        }

        return defaultCharset;
    }

    public static String parseCharset(Map<String, String> headers) {
        return parseCharset(headers, "ISO-8859-1");
    }
}
