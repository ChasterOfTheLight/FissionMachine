package com.devil.fission.machine.object.storage.core;

/**
 * 对象存储核心接口.
 *
 * @author devil
 * @date Created in 2022/4/26 9:23
 */
public interface StorageService<T> {
    
    /**
     * 创建一个空存储对象.
     *
     * @param path       文件相对路径
     * @param permission 文件权限 （StorablePermission）
     * @return 存储通用对象
     * @throws StorageException 存储异常
     */
    StorableObject create(String path, int permission) throws StorageException;
    
    /**
     * 根据文件相对路径获取存储通用对象.
     *
     * @param path 文件相对路径
     * @return 存储通用对象
     * @throws StorageException 存储异常
     */
    StorableObject get(String path) throws StorageException;
    
    /**
     * 获取完整访问路径.
     *
     * @param path 相对路径
     * @return 完整访问路径
     * @throws StorageException 存储异常
     */
    String getFullAccessPath(String path) throws StorageException;
    
    /**
     * 创建一个带有元数据（可能是空）的存储对象.
     *
     * @param path     文件相对路径
     * @param source   源对象（包含源数据和权限）
     * @param metaData 元数据
     * @return 存储通用对象
     * @throws StorageException 存储异常
     */
    StorableObject put(String path, StorableObject source, T metaData) throws StorageException;
    
    /**
     * 创建一个指定权限的存储对象.
     *
     * @param path       文件相对路径
     * @param source     源对象（包含源数据和权限）
     * @param permission 文件权限（StorablePermission）
     * @return 存储通用对象
     * @throws StorageException 存储异常
     */
    StorableObject put(String path, StorableObject source, int permission) throws StorageException;
    
    /**
     * 根据文件路径删除对象.
     *
     * @param path 文件相对路径
     * @throws StorageException 存储异常
     */
    void remove(String path) throws StorageException;
    
    /**
     * 根据文件路径设置文件权限.
     *
     * @param path       文件相对路径
     * @param permission 文件权限（StorablePermission）
     * @throws StorageException 存储异常
     */
    void setPermission(String path, int permission) throws StorageException;
    
    /**
     * 根据文件相对路径判断文件是否存在.
     *
     * @param path 文件相对路径
     * @return 是否存在
     * @throws StorageException 存储异常
     */
    boolean isExist(String path) throws StorageException;
    
}
