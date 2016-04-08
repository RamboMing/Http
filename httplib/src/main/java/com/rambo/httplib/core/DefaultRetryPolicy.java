package com.rambo.httplib.core;

import com.rambo.httplib.exception.HttpException;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class DefaultRetryPolicy implements RetryPolicy {
    private int mCurrentTimeoutMs;
    private int mCurrentRetryCount;
    private final int mMaxNumRetries;
    private final float mBackoffMultiplier;
    public static final int DEFAULT_TIMEOUT_MS = 10000;
    public static final int DEFAULT_MAX_RETRIES = 0;
    public static final float DEFAULT_BACKOFF_MULT = 1.0F;

    public DefaultRetryPolicy() {
        this(10000, 0, 1.0F);
    }

    public DefaultRetryPolicy(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier) {
        this.mCurrentTimeoutMs = initialTimeoutMs;
        this.mMaxNumRetries = maxNumRetries;
        this.mBackoffMultiplier = backoffMultiplier;
    }

    public int getCurrentTimeout() {
        return this.mCurrentTimeoutMs;
    }

    public int getCurrentRetryCount() {
        return this.mCurrentRetryCount;
    }

    public float getBackoffMultiplier() {
        return this.mBackoffMultiplier;
    }

    public void retry(HttpException error) throws HttpException {
        ++this.mCurrentRetryCount;
        this.mCurrentTimeoutMs = (int)((float)this.mCurrentTimeoutMs + (float)this.mCurrentTimeoutMs * this.mBackoffMultiplier);
        if(!this.hasAttemptRemaining()) {
            throw error;
        }
    }

    protected boolean hasAttemptRemaining() {
        return this.mCurrentRetryCount <= this.mMaxNumRetries;
    }
}

