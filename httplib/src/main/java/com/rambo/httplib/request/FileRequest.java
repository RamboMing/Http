package com.rambo.httplib.request;

import android.text.TextUtils;

import com.rambo.httplib.core.HttpHeaderParser;
import com.rambo.httplib.exception.HttpException;
import com.rambo.httplib.response.NetworkResponse;
import com.rambo.httplib.response.Response;
import com.rambo.httplib.response.URLHttpResponse;
import com.rambo.httplib.utils.HttpLog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import okhttp3.FormBody;
import okhttp3.RequestBody;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class FileRequest  extends Request {
    private final File mStoreFile;
    private final File mTemporaryFile;

    public FileRequest(FileRequestBuilder builder) {
        super(builder);
        this.mStoreFile = new File(builder.storePath);
        File folder = this.mStoreFile.getParentFile();
        if(folder != null && folder.mkdirs() && !this.mStoreFile.exists()) {
            try {
                this.mStoreFile.createNewFile();
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }

        this.mTemporaryFile = new File(builder.storePath + ".tmp");
    }

    public RequestBody getBody() {
        FormBody.Builder builder = new FormBody.Builder();
        this.addParams(builder);
        return builder.build();
    }

    public String getCacheKey() {
        return "";
    }

    public Response parseNetworkResponse(NetworkResponse response) {
        short errorCode;
        if(!this.isCanceled()) {
            if(this.mTemporaryFile.canRead() && this.mTemporaryFile.length() == response.getContentLength()) {
                if(this.mTemporaryFile.renameTo(this.mStoreFile)) {
                    return Response.success(response.data, response.headers, HttpHeaderParser.parseCacheHeaders(this, response));
                }

                errorCode = 1105;
            } else {
                errorCode = 1105;
            }
        } else {
            errorCode = 1107;
        }

        return Response.error(new HttpException(errorCode));
    }

    public Map<String, String> getHeaders() {
        this.header.put("Range", "bytes=" + this.mTemporaryFile.length() + "-");
        this.header.put("Accept-Encoding", "identity");
        return this.header;
    }

    public Map<String, String> putHeader(String k, String v) {
        if(this.header.keySet().contains(k)) {
            this.header.remove(k);
        }

        this.header.put(k, v);
        return this.header;
    }

    public static boolean isSupportRange(URLHttpResponse response) {
        if(TextUtils.equals(getHeader(response, "Accept-Ranges"), "bytes")) {
            return true;
        } else {
            String value = getHeader(response, "Content-Range");
            return value != null && value.startsWith("bytes");
        }
    }

    public static String getHeader(URLHttpResponse response, String key) {
        return (String)response.getHeaders().get(key);
    }

    public static boolean isGzipContent(URLHttpResponse response) {
        return TextUtils.equals(getHeader(response, "Content-Encoding"), "gzip");
    }

    public byte[] entityToBytes(URLHttpResponse response) throws IOException, HttpException {
        long fileSize = response.getContentLength();
        if(fileSize <= 0L) {
            HttpLog.d("Response doesn\'t present Content-Length!", new Object[0]);
        }

        long downloadedSize = this.mTemporaryFile.length();
        boolean isSupportRange = isSupportRange(response);
        if(isSupportRange) {
            fileSize += downloadedSize;
            String tmpFileRaf = (String)response.getHeaders().get("Content-Range");
            if(!TextUtils.isEmpty(tmpFileRaf)) {
                String in = "bytes " + downloadedSize + "-" + (fileSize - 1L);
                if(TextUtils.indexOf(tmpFileRaf, in) == -1) {
                    throw new IllegalStateException("The Content-Range Header is invalid Assume[" + in + "] vs Real[" + tmpFileRaf + "], " + "please remove the temporary file [" + this.mTemporaryFile + "].");
                }
            }
        }

        if(fileSize > 0L && this.mStoreFile.length() == fileSize) {
            this.mStoreFile.renameTo(this.mTemporaryFile);
            if(this.mListener != null) {
                this.mRequestQueue.getDelivery().postProgress(this.mListener, fileSize, fileSize);
            }

            return null;
        } else {
            RandomAccessFile tmpFileRaf1 = new RandomAccessFile(this.mTemporaryFile, "rw");
            if(isSupportRange) {
                tmpFileRaf1.seek(downloadedSize);
            } else {
                tmpFileRaf1.setLength(0L);
                downloadedSize = 0L;
            }

            Object in1 = response.getContentStream();

            try {
                if(isGzipContent(response) && !(in1 instanceof GZIPInputStream)) {
                    in1 = new GZIPInputStream((InputStream)in1);
                }

                byte[] e = new byte[6144];

                int offset;
                while((offset = ((InputStream)in1).read(e)) != -1) {
                    tmpFileRaf1.write(e, 0, offset);
                    downloadedSize += (long)offset;
                    if(this.mListener != null) {
                        this.mRequestQueue.getDelivery().postProgress(this.mListener, downloadedSize, fileSize);
                    }

                    if(this.isCanceled()) {
                        break;
                    }
                }
            } finally {
                ((InputStream)in1).close();

                try {
                    response.getContentStream().close();
                } catch (Exception var16) {
                    HttpLog.d("Error occured when calling consumingContent", new Object[0]);
                }

                tmpFileRaf1.close();
            }

            return null;
        }
    }

    public Priority getPriority() {
        return Priority.LOW;
    }
}
