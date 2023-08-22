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
    
    /**
     * 获取数据流.
     *
     * @throws IOException io异常
     * @return 数据流
     */
    InputStream getInputStream() throws IOException;
    
    /**
     * 获取存储键.
     *
     * @return 存储键
     */
    String getKey();
    
    /**
     * 获取权限.
     *
     * @return 权限
     */
    int getPermission();
}
