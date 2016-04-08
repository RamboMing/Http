package com.rambo.httplib.response;

import android.os.Handler;
import android.text.TextUtils;

import com.rambo.httplib.cache.Cache;
import com.rambo.httplib.cache.NoNetWorkCache;
import com.rambo.httplib.exception.HttpException;
import com.rambo.httplib.request.Request;

import java.io.File;
import java.util.concurrent.Executor;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class ExecutorDelivery implements ResponseDelivery {
    private final Executor mResponsePoster;

    public ExecutorDelivery(final Handler handler) {
        this.mResponsePoster = new Executor() {
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    public ExecutorDelivery(Executor executor) {
        this.mResponsePoster = executor;
    }

    public void postResponse(boolean isCache, Request<?> request, Response<?> response) {
        this.postResponse(isCache, request, response, (Runnable)null);
    }

    public void postResponse(boolean isCache, Request<?> request, Response<?> response, Runnable runnable) {
        request.markDelivered();
        request.addMarker("post-response");
        this.mResponsePoster.execute(new ExecutorDelivery.ResponseDeliveryRunnable(isCache, request, response, runnable));
    }

    public void postError(boolean isCache, Request<?> request, HttpException error) {
        request.addMarker("post-error");
        Response response = Response.error(error);
        this.mResponsePoster.execute(new ExecutorDelivery.ResponseDeliveryRunnable(isCache, request, response, (Runnable)null));
    }

    public void postProgress(final HttpResponseListnener listener, final long transferredBytes, final long totalSize) {
        this.mResponsePoster.execute(new Runnable() {
            public void run() {
                listener.onProgress(transferredBytes, totalSize, (float)((int)((float)transferredBytes * 1.0F / (float)totalSize) * 100));
            }
        });
    }

    private class ResponseDeliveryRunnable implements Runnable {
        private final Request mRequest;
        private final Response mResponse;
        private final Runnable mRunnable;
        private boolean mIsCache;

        public ResponseDeliveryRunnable(boolean isCache, Request request, Response response, Runnable runnable) {
            this.mRequest = request;
            this.mResponse = response;
            this.mRunnable = runnable;
            this.mIsCache = isCache;
        }

        public void run() {
            if(this.mRequest.isCanceled()) {
                this.mRequest.finish("canceled-at-delivery");
            } else {
                if(this.mResponse.isSuccess()) {
                    File error = !TextUtils.isEmpty(this.mRequest.getStorePath())?new File(this.mRequest.getStorePath()):null;
                    this.mRequest.deliverResponse(this.mIsCache, this.mResponse.headers, error != null && error.exists()?error:this.mResponse.result);
                } else {
                    HttpException error1 = this.mResponse.error;
                    int errorCode = error1.getErrorCode();
                    if((errorCode == 1103 || errorCode == 1101 || errorCode == 1102) && this.mRequest.isForcache()) {
                        Cache cache = this.mRequest.getRequestQueue().getCache();
                        if(cache != null && cache instanceof NoNetWorkCache) {
                            HttpResponseListnener listener = this.mRequest.getListener();
                            NoNetWorkCache noNetWorkCache = (NoNetWorkCache)cache;
                            noNetWorkCache.cache(this.mRequest, listener, new HttpException(1103));
                            noNetWorkCache.cache(this.mRequest, listener, this.mResponse.error);
                        } else {
                            this.mRequest.deliverError(this.mResponse.error);
                        }
                    } else {
                        this.mRequest.deliverError(this.mResponse.error);
                    }
                }

                if(this.mResponse.intermediate) {
                    this.mRequest.addMarker("intermediate-response");
                } else {
                    this.mRequest.finish("done");
                }

                if(this.mRunnable != null) {
                    this.mRunnable.run();
                }

            }
        }
    }
}
