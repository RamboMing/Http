package com.rambo.httplib.core;

import android.annotation.TargetApi;
import android.net.TrafficStats;
import android.os.Build;
import android.os.SystemClock;

import com.rambo.httplib.cache.Cache;
import com.rambo.httplib.exception.HttpException;
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
public class NetworkDispatcher
        extends Thread {
    private final BlockingQueue<Request<?>> mQueue;
    private final Network mNetwork;
    private final Cache mCache;
    private final ResponseDelivery mDelivery;
    private volatile boolean mQuit = false;

    public NetworkDispatcher(BlockingQueue<Request<?>> queue, Network network, Cache cache, ResponseDelivery delivery) {
        this.mQueue = queue;
        this.mNetwork = network;
        this.mCache = cache;
        this.mDelivery = delivery;
    }

    public void quit() {
        this.mQuit = true;
        this.interrupt();
    }

    @TargetApi(14)
    private void addTrafficStatsTag(Request<?> request) {
        if (Build.VERSION.SDK_INT >= 14) {
            TrafficStats.setThreadStatsTag(request.getTrafficStatsTag());
        }

    }

    public void run() {
        android.os.Process.setThreadPriority(10);

        while (true) {
            Request request;
            while (true) {
                long startTimeMs = SystemClock.elapsedRealtime();

                try {
                    request = (Request) this.mQueue.take();
                    break;
                } catch (InterruptedException var6) {
                    if (this.mQuit) {
                        return;
                    }
                }
            }

            try {
                request.addMarker("network-queue-take");
                if (request.isCanceled()) {
                    request.finish("network-discard-cancelled");
                } else {
                    this.addTrafficStatsTag(request);
                    request.deliverStart();
                    NetworkResponse e = this.mNetwork.performRequest(request);
                    request.addMarker("network-http-complete");
                    if (e.notModified && request.hasHadResponseDelivered()) {
                        request.finish("not-modified");
                    } else {
                        Response volleyError1 = request.parseNetworkResponse(e);
                        request.addMarker("network-parse-complete");
                        if (request.shouldCache() && volleyError1.cacheEntry != null) {
                            this.mCache.put(request.getCacheKey(), volleyError1.cacheEntry);
                            request.addMarker("network-cache-written");
                        }

                        request.markDelivered();
                        this.mDelivery.postResponse(false, request, volleyError1);
                    }
                }
            } catch (HttpException var7) {
                this.parseAndDeliverNetworkError(request, var7);
            } catch (Exception var8) {
                HttpLog.e(var8, "Unhandled exception %s", new Object[]{var8.toString()});
                HttpException volleyError = new HttpException(var8);
                this.mDelivery.postError(false, request, volleyError);
            }
        }
    }

    private void parseAndDeliverNetworkError(Request<?> request, HttpException error) {
        error = request.parseNetworkError(error);
        this.mDelivery.postError(false, request, error);
    }
}
