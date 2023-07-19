package com.devil.fission.machine.object.storage.core;

import java.io.InputStream;

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
