package com.devil.fission.machine.object.storage.local;

import com.devil.fission.machine.object.storage.core.StorablePermission;
import com.devil.fission.machine.object.storage.core.StorableObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * 本地对象存储对象.
 *
 * @author devil
 * @date Created in 2022/4/26 9:34
 */
public class LocalFileStorableObject implements StorableObject {
    
    protected String path;
    
    protected String fileName;
    
    protected File file;
    
    public LocalFileStorableObject(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;
        file = new File(path, fileName);
    }
    
    public LocalFileStorableObject(String path, String fileName, int permission) {
        this.path = path;
        this.fileName = fileName;
        file = new File(path, fileName);
        file.setWritable(StorablePermission.isWritable(permission));
        file.setReadable(StorablePermission.isReadable(permission));
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(file.toPath());
    }
    
    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(file.toPath());
    }
    
    @Override
    public String getKey() {
        return fileName;
    }
    
    @Override
    public int getPermission() {
        return StorablePermission.compute(file.canRead(), file.canWrite());
    }
    
    public File getFile() {
        return file;
    }
}
