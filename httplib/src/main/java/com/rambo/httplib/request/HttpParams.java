package com.rambo.httplib.request;

import android.text.TextUtils;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class HttpParams {
    private Map<String, String> urlParams = new LinkedHashMap();
    private List<FormFile> fileParams = new LinkedList();

    public HttpParams() {
    }

    public void put(String key, String value) {
        if (!TextUtils.isEmpty(key)) {
            this.urlParams.put(key, value);
        }
    }

    public void put(String key, boolean value) {
        this.put(key, value + "");
    }

    public void put(String key, int value) {
        this.put(key, value + "");
    }

    public void put(String key, float value) {
        this.put(key, value + "");
    }

    public void put(String key, long value) {
        this.put(key, value + "");
    }

    public void put(Map<String, String> params) {
        if (params != null) {
            Iterator var2 = params.keySet().iterator();

            while (var2.hasNext()) {
                String key = (String) var2.next();
                this.put(key, (String) params.get(key));
            }
        }

    }

    public void addFile(String key, String filePath) {
        if (TextUtils.isEmpty(filePath) || !(new File(filePath)).exists()) {
            new IllegalArgumentException("file paht is null or file not exsit");
        }

        FormFile formFile = new FormFile(TextUtils.isEmpty(key) ? "file" : key, filePath);
        this.fileParams.add(formFile);
    }

    public void addFiles(String key, List<String> filePaths) {
        if (filePaths != null && !filePaths.isEmpty()) {
            Iterator var3 = filePaths.iterator();

            while (var3.hasNext()) {
                String filePath = (String) var3.next();
                this.addFile(key, filePath);
            }
        }

    }

    public void addFile(String key, File file) {
        if (file != null && file.exists()) {
            this.addFile(key, file.getAbsolutePath());
        }

    }

    public void addFiles(List<String> filePaths) {
        if (filePaths != null && !filePaths.isEmpty()) {
            Iterator var2 = filePaths.iterator();

            while (var2.hasNext()) {
                String filePath = (String) var2.next();
                this.addFile((String) null, (String) filePath);
            }
        }

    }

    public void addFile(String filePath) {
        this.addFile((String) null, (String) filePath);
    }

    public void addFile(File file) {
        this.addFile((String) null, (File) file);
    }

    public Map<String, String> getUrlParams() {
        return this.urlParams;
    }

    public List<FormFile> getFileParams() {
        return this.fileParams;
    }

    public String getUrlParamsStr() {
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;

        String key;
        for (Iterator var3 = this.urlParams.keySet().iterator(); var3.hasNext(); result.append(key).append("=").append((String) this.urlParams.get(key))) {
            key = (String) var3.next();
            if (!isFirst) {
                result.append("&");
            } else {
                result.append("?");
                isFirst = false;
            }
        }

        return result.toString();
    }
}
