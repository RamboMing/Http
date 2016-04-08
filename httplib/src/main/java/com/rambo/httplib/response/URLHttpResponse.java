package com.rambo.httplib.response;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.InputStream;
import java.util.HashMap;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class URLHttpResponse implements Parcelable {
    private static final long serialVersionUID = 1L;
    private HashMap<String, String> headers;
    private int responseCode;
    private String responseMessage;
    private InputStream contentStream;
    private String contentEncoding;
    private String contentType;
    private long contentLength;
    public static final Creator<URLHttpResponse> CREATOR = new Creator() {
        public URLHttpResponse createFromParcel(Parcel source) {
            return new URLHttpResponse(source);
        }

        public URLHttpResponse[] newArray(int size) {
            return new URLHttpResponse[size];
        }
    };

    public HashMap<String, String> getHeaders() {
        return this.headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public InputStream getContentStream() {
        return this.contentStream;
    }

    public void setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
    }

    public String getContentEncoding() {
        return this.contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.headers);
        dest.writeInt(this.responseCode);
        dest.writeString(this.responseMessage);
        dest.writeString(this.contentEncoding);
        dest.writeString(this.contentType);
        dest.writeLong(this.contentLength);
    }

    public URLHttpResponse() {
    }

    protected URLHttpResponse(Parcel in) {
        this.headers = (HashMap)in.readSerializable();
        this.responseCode = in.readInt();
        this.responseMessage = in.readString();
        this.contentEncoding = in.readString();
        this.contentType = in.readString();
        this.contentLength = in.readLong();
    }
}

