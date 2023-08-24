package com.devil.fission.machine.object.storage.core;

import java.io.InputStream;

/**
 * 存储请求.
 *
 * @author devil
 * @date Created in 2023/8/22 17:24
 */
public class StorableRequest {

    private InputStream inputStream;
    
    private String fileName;
    
    private String objectName;

    public StorableRequest() {
    }

    public StorableRequest(InputStream inputStream, String fileName, String objectName) {
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.objectName = objectName;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
}
