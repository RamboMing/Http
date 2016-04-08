package com.rambo.httplib.core;

import com.rambo.httplib.cache.Cache;
import com.rambo.httplib.request.Request;
import com.rambo.httplib.response.NetworkResponse;
import com.rambo.httplib.response.Response;
import com.rambo.httplib.response.ResponseDelivery;
import com.rambo.httplib.utils.HttpLog;

import java.util.concurrent.BlockingQueue;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class CacheDispatcher extends Thread {
    private static final boolean DEBUG;
    private final BlockingQueue<Request<?>> mCacheQueue;
    private final BlockingQueue<Request<?>> mNetworkQueue;
    private final Cache mCache;
    private final ResponseDelivery mDelivery;
    private volatile boolean mQuit = false;

    public CacheDispatcher(BlockingQueue<Request<?>> cacheQueue, BlockingQueue<Request<?>> networkQueue, Cache cache, ResponseDelivery delivery) {
        this.mCacheQueue = cacheQueue;
        this.mNetworkQueue = networkQueue;
        this.mCache = cache;
        this.mDelivery = delivery;
    }

    public void quit() {
        this.mQuit = true;
        this.interrupt();
    }

    public void run() {
        if(DEBUG) {
            HttpLog.v("start new dispatcher", new Object[0]);
        }

        android.os.Process.setThreadPriority(10);
        this.mCache.initialize();

        while(true) {
            while(true) {
                while(true) {
                    while(true) {
                        try {
                            final Request e = (Request)this.mCacheQueue.take();
                            e.addMarker("cache-queue-take");
                            if(e.isCanceled()) {
                                e.finish("cache-discard-canceled");
                            } else {
                                Cache.Entry entry = this.mCache.get(e.getCacheKey());
                                if(entry == null) {
                                    e.addMarker("cache-miss");
                                    this.mNetworkQueue.put(e);
                                } else if(!entry.isExpired()) {
                                    e.addMarker("cache-hit");
                                    Response response = e.parseNetworkResponse(new NetworkResponse(entry.data != null?(long)entry.data.length:0L, entry.data, entry.responseHeaders));
                                    e.addMarker("cache-hit-parsed");
                                    if(!entry.refreshNeeded()) {
                                        this.mDelivery.postResponse(true, e, response);
                                    } else {
                                        e.addMarker("cache-hit-refresh-needed");
                                        e.setCacheEntry(entry);
                                        response.intermediate = true;
                                        this.mDelivery.postResponse(true, e, response, new Runnable() {
                                            public void run() {
                                                try {
                                                    CacheDispatcher.this.mNetworkQueue.put(e);
                                                } catch (InterruptedException var2) {
                                                    ;
                                                }

                                            }
                                        });
                                    }
                                } else {
                                    e.addMarker("cache-hit-expired");
                                    e.setCacheEntry(entry);
                                    this.mNetworkQueue.put(e);
                                }
                            }
                        } catch (InterruptedException var4) {
                            if(this.mQuit) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    static {
        DEBUG = HttpLog.DEBUG;
    }
}
