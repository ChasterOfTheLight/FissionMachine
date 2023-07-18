package com.devil.fission.machine.object.storage.core;

/**
 * 对象存储操作权限.
 *
 * @author devil
 * @date Created in 2022/4/26 9:29
 */
public class StorablePermission {
    
    public static final int READABLE = 1;
    
    public static final int WRITABLE = 2;
    
    public static final int PRIVATE = 0;
    
    /**
     * 实际值3 (01 | 10 = 11).
     */
    public static final int PUBLIC = READABLE | WRITABLE;
    
    public static boolean isReadable(int value) {
        return (value & READABLE) > 0;
    }
    
    public static boolean isWritable(int value) {
        return (value & WRITABLE) > 0;
    }
    
    public static int compute(boolean readable, boolean writable) {
        return (readable ? READABLE : 0) | (writable ? WRITABLE : 0);
    }
}
