package com.rambo.httplib.cookie;

import android.text.TextUtils;

import com.rambo.httplib.cache.Cache;
import com.rambo.httplib.utils.JsonUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class OKCookieJar implements CookieJar {
    private Cache cookieCache;

    public OKCookieJar(Cache cookiesCache) {
        this.cookieCache = cookiesCache;
    }

    public void saveFromResponse(HttpUrl httpUrl, List<Cookie> cookies) {
        if(this.cookieCache != null && cookies != null) {
            long now = System.currentTimeMillis();
            this.cookieCache.initialize();
            long serverDate = 0L;
            long lastModified = 0L;
            long maxAge = 0L;
            long softExpire = 1000L * maxAge + now;
            long finalExpire = 1000L * maxAge + now;
            String url = httpUrl.toString();
            this.cookieCache.remove(url);
            ArrayList okWrapCookies = new ArrayList();
            Iterator entry = cookies.iterator();

            while(entry.hasNext()) {
                Cookie cookie = (Cookie)entry.next();
                OkWrapCookie okWrapCookie = this.convertWrapCookie(cookie);
                okWrapCookies.add(okWrapCookie);
            }

            this.cookieCache.remove(url);
            Cache.Entry entry1 = new Cache.Entry();
            entry1.data = JsonUtil.toJson(okWrapCookies).getBytes();
            entry1.softTtl = softExpire;
            entry1.ttl = finalExpire;
            entry1.serverDate = serverDate;
            entry1.lastModified = lastModified;
            this.cookieCache.put(url, entry1);
        }
    }

    public List<Cookie> loadForRequest(HttpUrl httpUrl) {
        ArrayList cookies = new ArrayList();

        try {
            if(this.cookieCache == null) {
                return cookies;
            }

            this.cookieCache.initialize();
            String e = httpUrl.toString();
            Cache.Entry entry = this.cookieCache.get(e);
            if(entry != null) {
                byte[] data = entry.data;
                if(data != null && data.length > 0) {
                    List existcookies = JsonUtil.fromJsonList(new String(data, "utf-8"), OkWrapCookie.class);
                    if(existcookies != null && !existcookies.isEmpty()) {
                        Iterator var7 = existcookies.iterator();

                        while(var7.hasNext()) {
                            OkWrapCookie okWrapCookie = (OkWrapCookie)var7.next();
                            cookies.add(this.convertCookie(okWrapCookie));
                        }
                    }
                }
            }
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        return cookies;
    }

    private OkWrapCookie convertWrapCookie(Cookie cookie) {
        return new OkWrapCookie(cookie.name(), cookie.value(), cookie.expiresAt(), cookie.domain(), cookie.path(), cookie.secure(), cookie.httpOnly(), cookie.persistent(), cookie.hostOnly());
    }

    private Cookie convertCookie(OkWrapCookie cookie) {
        Cookie.Builder builder = new Cookie.Builder();
        if(!TextUtils.isEmpty(cookie.getName())) {
            builder.name(cookie.getName());
        }

        if(!TextUtils.isEmpty(cookie.getValue())) {
            builder.value(cookie.getValue());
        }

        if(cookie.getExpiresAt() >= 0L) {
            builder.expiresAt(cookie.getExpiresAt());
        }

        if(!TextUtils.isEmpty(cookie.getDomain())) {
            builder.name(cookie.getDomain());
        }

        if(!TextUtils.isEmpty(cookie.getPath())) {
            builder.path(cookie.getPath());
        }

        if(cookie.isSecure()) {
            builder.secure();
        }

        String domain;
        if(cookie.isHostOnly()) {
            domain = cookie.getDomain();
            if(!TextUtils.isEmpty(domain)) {
                builder.hostOnlyDomain(domain);
            }
        } else {
            domain = cookie.getDomain();
            if(!TextUtils.isEmpty(domain)) {
                builder.domain(domain);
            }
        }

        return builder.build();
    }
}

