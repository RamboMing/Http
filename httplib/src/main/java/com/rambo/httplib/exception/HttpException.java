package com.rambo.httplib.exception;

import android.text.TextUtils;


/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class HttpException  extends Exception {
    private int errorCode;
    private String message;
    private Throwable throwable;

    public HttpException(int errorCode) {
        this.errorCode = errorCode;
    }

    public HttpException(String message) {
        this.message = message;
    }

    public HttpException(Throwable errorCode) {
        super(errorCode);
        this.throwable = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getMessage() {
        return this.errorCode > 0? ErrorCode.getErrorMessage(this.errorCode):(!TextUtils.isEmpty(this.message)?this.message:super.getMessage());
    }
}

