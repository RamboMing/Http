package com.rambo.httplib.cache;

import android.os.AsyncTask;

import com.rambo.httplib.exception.HttpException;
import com.rambo.httplib.request.Request;
import com.rambo.httplib.response.HttpResponseListnener;

import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public abstract class NoNetWorkCache<T> extends Cache {
    public NoNetWorkCache() {
    }

    public void cache(final Request request, final HttpResponseListnener listnener, final HttpException httpException) {
        new AsyncTask<Object, Void, T>() {
            @Override
            protected T doInBackground(Object... params) {
                try {
                    return NoNetWorkCache.this.getCache(request, listnener, httpException);
                } catch (Exception var3) {
                    var3.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(T result) {
                super.onPostExecute(result);
                if (result != null) {
                    listnener.onSuccess(true, (Map) null, result);
                } else {
                    listnener.onFailure(httpException);
                }
            }
        }.execute();

      /*  RxAysnTaskManger rxAysnTaskManger = RxAysnTaskManger.getInstance();
        rxAysnTaskManger.publishTask(new RxAysnTask(new Object[0]) {
            public T runInBackground(Object... objs) {
                try {
                    return NoNetWorkCache.this.getCache(request, listnener, httpException);
                } catch (Exception var3) {
                    var3.printStackTrace();
                    return null;
                }
            }

            public void onResult(boolean iscancle, T result) {
                super.onResult(iscancle, result);
                if(result != null) {
                    listnener.onSuccess(true, (Map)null, result);
                } else {
                    listnener.onFailure(httpException);
                }

            }
        });*/
    }

    public abstract T getCache(Request var1, HttpResponseListnener var2, HttpException var3);
}
