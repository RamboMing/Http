package com.rambo.httplib.cache;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class NoCache extends Cache {
    public NoCache() {
    }

    public void clear() {
    }

    public Entry get(String key) {
        return null;
    }

    public void put(String key, Entry entry) {
    }

    public void invalidate(String key, boolean fullExpire) {
    }

    public void remove(String key) {
    }

    public void initialize() {
    }
}
