package com.rambo.httplib.cache;

import java.util.Collections;
import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public abstract class Cache {
    public Cache() {
    }

    public abstract Cache.Entry get(String var1);

    public abstract void put(String var1, Cache.Entry var2);

    public abstract void initialize();

    public abstract void invalidate(String var1, boolean var2);

    public abstract void remove(String var1);

    public abstract void clear();

    public static class Entry {
        public byte[] data;
        public String etag;
        public long serverDate;
        public long lastModified;
        public long ttl;
        public long softTtl;
        public Map<String, String> responseHeaders = Collections.emptyMap();

        public Entry() {
        }

        public boolean isExpired() {
            return this.ttl < System.currentTimeMillis();
        }

        public boolean refreshNeeded() {
            return this.softTtl < System.currentTimeMillis();
        }
    }
}
