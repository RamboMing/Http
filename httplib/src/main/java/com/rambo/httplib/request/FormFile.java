package com.rambo.httplib.request;

import java.io.File;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author: lanming
 * @date: 2016-04-08
 */
public class FormFile {
    private String key;
    private String filePath;

    public FormFile(String key, String filePath) {
        this.key = key;
        this.filePath = filePath;
        this.filePath = filePath;
    }

    public File getFile() {
        File file = new File(this.filePath);
        return file;
    }

    public String getFileName() {
        File tempFile = new File(this.filePath.trim());
        return tempFile.getName();
    }

    public String getKey() {
        return this.key;
    }
}
