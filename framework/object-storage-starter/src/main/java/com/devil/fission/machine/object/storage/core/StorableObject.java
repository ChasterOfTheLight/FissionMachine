package com.devil.fission.machine.object.storage.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * 对象存储核心对象接口.
 *
 * @author devil
 * @date Created in 2022/4/26 9:23
 */
public interface StorableObject {
    
    InputStream getInputStream() throws IOException;
    
    String getKey();
    
    int getPermission();
}
