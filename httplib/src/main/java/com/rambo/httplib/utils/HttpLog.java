package com.rambo.httplib.utils;

import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class HttpLog {
    public static String TAG = "Volley";
    public static boolean DEBUG;

    public HttpLog() {
    }

    public static void setTag(String tag) {
        d("Changing log tag to %s", new Object[]{tag});
        TAG = tag;
        DEBUG = Log.isLoggable(TAG, 2);
    }

    public static void v(String format, Object... args) {
        if(DEBUG) {
            Log.v(TAG, buildMessage(format, args));
        }

    }

    public static void d(String format, Object... args) {
        Log.d(TAG, buildMessage(format, args));
    }

    public static void e(String format, Object... args) {
        Log.e(TAG, buildMessage(format, args));
    }

    public static void e(Throwable tr, String format, Object... args) {
        Log.e(TAG, buildMessage(format, args), tr);
    }

    public static void wtf(String format, Object... args) {
        Log.wtf(TAG, buildMessage(format, args));
    }

    public static void wtf(Throwable tr, String format, Object... args) {
        Log.wtf(TAG, buildMessage(format, args), tr);
    }

    private static String buildMessage(String format, Object... args) {
        String msg = args == null?format:String.format(Locale.US, format, args);
        StackTraceElement[] trace = (new Throwable()).fillInStackTrace().getStackTrace();
        String caller = "<unknown>";

        for(int i = 2; i < trace.length; ++i) {
            Class clazz = trace[i].getClass();
            if(!clazz.equals(HttpLog.class)) {
                String callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass.lastIndexOf(46) + 1);
                callingClass = callingClass.substring(callingClass.lastIndexOf(36) + 1);
                caller = callingClass + "." + trace[i].getMethodName();
                break;
            }
        }

        return String.format(Locale.US, "[%d] %s: %s", new Object[]{Long.valueOf(Thread.currentThread().getId()), caller, msg});
    }

    static {
        DEBUG = Log.isLoggable(TAG, 2);
    }

    public static class MarkerLog {
        public static final boolean ENABLED;
        private static final long MIN_DURATION_FOR_LOGGING_MS = 0L;
        private final List<Marker> mMarkers = new ArrayList();
        private boolean mFinished = false;

        public MarkerLog() {
        }

        public synchronized void add(String name, long threadId) {
            if(this.mFinished) {
                throw new IllegalStateException("Marker added to finished log");
            } else {
                this.mMarkers.add(new HttpLog.MarkerLog.Marker(name, threadId, SystemClock.elapsedRealtime()));
            }
        }

        public synchronized void finish(String header) {
            this.mFinished = true;
            long duration = this.getTotalDuration();
            if(duration > 0L) {
                long prevTime = ((HttpLog.MarkerLog.Marker)this.mMarkers.get(0)).time;
                HttpLog.d("(%-4d ms) %s", new Object[]{Long.valueOf(duration), header});

                long thisTime;
                for(Iterator var6 = this.mMarkers.iterator(); var6.hasNext(); prevTime = thisTime) {
                    HttpLog.MarkerLog.Marker marker = (HttpLog.MarkerLog.Marker)var6.next();
                    thisTime = marker.time;
                    HttpLog.d("(+%-4d) [%2d] %s", new Object[]{Long.valueOf(thisTime - prevTime), Long.valueOf(marker.thread), marker.name});
                }

            }
        }

        protected void finalize() throws Throwable {
            if(!this.mFinished) {
                this.finish("Request on the loose");
                HttpLog.e("Marker log finalized without finish() - uncaught exit point for request", new Object[0]);
            }

        }

        private long getTotalDuration() {
            if(this.mMarkers.size() == 0) {
                return 0L;
            } else {
                long first = ((HttpLog.MarkerLog.Marker)this.mMarkers.get(0)).time;
                long last = ((HttpLog.MarkerLog.Marker)this.mMarkers.get(this.mMarkers.size() - 1)).time;
                return last - first;
            }
        }

        static {
            ENABLED = HttpLog.DEBUG;
        }

        private static class Marker {
            public final String name;
            public final long thread;
            public final long time;

            public Marker(String name, long thread, long time) {
                this.name = name;
                this.thread = thread;
                this.time = time;
            }
        }
    }
}
