package com.rambo.httplib.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class ByteArrayPool {
    private final List<byte[]> mBuffersByLastUse = new LinkedList();
    private final List<byte[]> mBuffersBySize = new ArrayList(64);
    private int mCurrentSize = 0;
    private final int mSizeLimit;
    protected static final Comparator<byte[]> BUF_COMPARATOR = new Comparator<byte[]>() {
        public int compare(byte[] lhs, byte[] rhs) {
            return lhs.length - rhs.length;
        }
    };
    private static ByteArrayPool mPool = new ByteArrayPool(4096);

    public ByteArrayPool(int sizeLimit) {
        this.mSizeLimit = sizeLimit;
    }

    public static ByteArrayPool get() {
        return mPool;
    }

    public static void init(int poolSize) {
        mPool = new ByteArrayPool(poolSize);
    }

    public synchronized byte[] getBuf(int len) {
        for (int i = 0; i < this.mBuffersBySize.size(); ++i) {
            byte[] buf = (byte[]) this.mBuffersBySize.get(i);
            if (buf.length >= len) {
                this.mCurrentSize -= buf.length;
                this.mBuffersBySize.remove(i);
                this.mBuffersByLastUse.remove(buf);
                return buf;
            }
        }

        return new byte[len];
    }

    public synchronized void returnBuf(byte[] buf) {
        if (buf != null && buf.length <= this.mSizeLimit) {
            this.mBuffersByLastUse.add(buf);
            int pos = Collections.binarySearch(this.mBuffersBySize, buf, BUF_COMPARATOR);
            if (pos < 0) {
                pos = -pos - 1;
            }

            this.mBuffersBySize.add(pos, buf);
            this.mCurrentSize += buf.length;
            this.trim();
        }
    }

    private synchronized void trim() {
        while (this.mCurrentSize > this.mSizeLimit) {
            byte[] buf = (byte[]) this.mBuffersByLastUse.remove(0);
            this.mBuffersBySize.remove(buf);
            this.mCurrentSize -= buf.length;
        }

    }
}
