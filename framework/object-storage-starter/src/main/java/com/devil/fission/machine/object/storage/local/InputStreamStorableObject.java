package com.devil.fission.machine.object.storage.local;

import com.devil.fission.machine.object.storage.core.StorableObject;
import com.devil.fission.machine.object.storage.core.StorablePermission;

import java.io.IOException;
import java.io.InputStream;

/**
 * 流存储对象.
 *
 * @author Devil
 * @date Created in 2023/7/18 14:29
 */
public class InputStreamStorableObject implements StorableObject {
    
    private final InputStream inputStream;
    
    public InputStreamStorableObject(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }
    
    @Override
    public String getKey() {
        return null;
    }
    
    @Override
    public int getPermission() {
        return StorablePermission.PUBLIC;
    }
}
