package com.rambo.httplib.core;

import android.os.Handler;
import android.os.Looper;

import com.rambo.httplib.cache.Cache;
import com.rambo.httplib.request.Request;
import com.rambo.httplib.response.ExecutorDelivery;
import com.rambo.httplib.response.ResponseDelivery;
import com.rambo.httplib.utils.HttpLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class RequestQueue {
    private AtomicInteger mSequenceGenerator;
    private final Map<String, Queue<Request<?>>> mWaitingRequests;
    private final Set<Request<?>> mCurrentRequests;
    private final PriorityBlockingQueue<Request<?>> mCacheQueue;
    private final PriorityBlockingQueue<Request<?>> mNetworkQueue;
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;
    private final Cache mCache;
    private final Network mNetwork;
    private final ResponseDelivery mDelivery;
    private NetworkDispatcher[] mDispatchers;
    private CacheDispatcher mCacheDispatcher;
    private List<RequestFinishedListener> mFinishedListeners;

    public RequestQueue(Cache cache, Network network, int threadPoolSize, ResponseDelivery delivery) {
        this.mSequenceGenerator = new AtomicInteger();
        this.mWaitingRequests = new HashMap();
        this.mCurrentRequests = new HashSet();
        this.mCacheQueue = new PriorityBlockingQueue();
        this.mNetworkQueue = new PriorityBlockingQueue();
        this.mFinishedListeners = new ArrayList();
        this.mCache = cache;
        this.mNetwork = network;
        this.mDispatchers = new NetworkDispatcher[threadPoolSize];
        this.mDelivery = delivery;
    }

    public RequestQueue(Cache cache, Network network, int threadPoolSize) {
        this(cache, network, threadPoolSize, new ExecutorDelivery(new Handler(Looper.getMainLooper())));
    }

    public RequestQueue(Cache cache, Network network) {
        this(cache, network, 4);
    }

    public void start() {
        this.stop();
        this.mCacheDispatcher = new CacheDispatcher(this.mCacheQueue, this.mNetworkQueue, this.mCache, this.mDelivery);
        this.mCacheDispatcher.start();

        for (int i = 0; i < this.mDispatchers.length; ++i) {
            NetworkDispatcher networkDispatcher = new NetworkDispatcher(this.mNetworkQueue, this.mNetwork, this.mCache, this.mDelivery);
            this.mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }

    }

    public void stop() {
        if (this.mCacheDispatcher != null) {
            this.mCacheDispatcher.quit();
        }

        for (int i = 0; i < this.mDispatchers.length; ++i) {
            if (this.mDispatchers[i] != null) {
                this.mDispatchers[i].quit();
            }
        }

    }

    public int getSequenceNumber() {
        return this.mSequenceGenerator.incrementAndGet();
    }

    public Cache getCache() {
        return this.mCache;
    }

    public void cancelAll(RequestQueue.RequestFilter filter) {
        Set var2 = this.mCurrentRequests;
        synchronized (this.mCurrentRequests) {
            Iterator var3 = this.mCurrentRequests.iterator();

            while (var3.hasNext()) {
                Request request = (Request) var3.next();
                if (filter.apply(request)) {
                    request.cancel();
                }
            }

        }
    }

    public void cancelAll(final Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Cannot cancelAll with a null tag");
        } else {
            this.cancelAll(new RequestQueue.RequestFilter() {
                public boolean apply(Request<?> request) {
                    return request.getTag() == tag;
                }
            });
        }
    }

    public <T> Request<T> add(Request<T> request) {
        request.setRequestQueue(this);
        Set var2 = this.mCurrentRequests;
        synchronized (this.mCurrentRequests) {
            this.mCurrentRequests.add(request);
        }

        request.setSequence(this.getSequenceNumber());
        request.addMarker("add-to-queue");
        if (!request.shouldFromCache()) {
            this.mNetworkQueue.add(request);
            return request;
        } else {
            Map var8 = this.mWaitingRequests;
            synchronized (this.mWaitingRequests) {
                String cacheKey = request.getCacheKey();
                if (this.mWaitingRequests.containsKey(cacheKey)) {
                    Object stagedRequests = (Queue) this.mWaitingRequests.get(cacheKey);
                    if (stagedRequests == null) {
                        stagedRequests = new LinkedList();
                    }

                    ((Queue) stagedRequests).add(request);
                    this.mWaitingRequests.put(cacheKey, (Queue)stagedRequests);
                    if (HttpLog.DEBUG) {
                        HttpLog.v("Request for cacheKey=%s is in flight, putting on hold.", new Object[]{cacheKey});
                    }
                } else {
                    this.mWaitingRequests.put(cacheKey, (Queue) null);
                    this.mCacheQueue.add(request);
                }

                return request;
            }
        }
    }

    public <T> void finish(Request<T> request) {
        Set var2 = this.mCurrentRequests;
        synchronized (this.mCurrentRequests) {
            this.mCurrentRequests.remove(request);
        }

        List var10 = this.mFinishedListeners;
        synchronized (this.mFinishedListeners) {
            Iterator cacheKey = this.mFinishedListeners.iterator();

            while (true) {
                if (!cacheKey.hasNext()) {
                    break;
                }

                RequestQueue.RequestFinishedListener waitingRequests = (RequestQueue.RequestFinishedListener) cacheKey.next();
                waitingRequests.onRequestFinished(request);
            }
        }

        if (request.shouldCache()) {
            Map var11 = this.mWaitingRequests;
            synchronized (this.mWaitingRequests) {
                String cacheKey1 = request.getCacheKey();
                Queue waitingRequests1 = (Queue) this.mWaitingRequests.remove(cacheKey1);
                if (waitingRequests1 != null) {
                    if (HttpLog.DEBUG) {
                        HttpLog.v("Releasing %d waiting requests for cacheKey=%s.", new Object[]{Integer.valueOf(waitingRequests1.size()), cacheKey1});
                    }

                    this.mCacheQueue.addAll(waitingRequests1);
                }
            }
        }

    }

    public <T> void addRequestFinishedListener(RequestQueue.RequestFinishedListener<T> listener) {
        List var2 = this.mFinishedListeners;
        synchronized (this.mFinishedListeners) {
            this.mFinishedListeners.add(listener);
        }
    }

    public <T> void removeRequestFinishedListener(RequestQueue.RequestFinishedListener<T> listener) {
        List var2 = this.mFinishedListeners;
        synchronized (this.mFinishedListeners) {
            this.mFinishedListeners.remove(listener);
        }
    }

    public ResponseDelivery getDelivery() {
        return this.mDelivery;
    }

    public interface RequestFilter {
        boolean apply(Request<?> var1);
    }

    public interface RequestFinishedListener<T> {
        void onRequestFinished(Request<T> var1);
    }
}
