package com.devil.fission.machine.object.storage.local;

import com.devil.fission.machine.object.storage.core.StorablePermission;
import com.devil.fission.machine.object.storage.core.StorableObject;
import com.devil.fission.machine.object.storage.core.StorageErrorCode;
import com.devil.fission.machine.object.storage.core.StorageException;
import com.devil.fission.machine.object.storage.core.StorageService;

import java.io.*;

/**
 * 本地对象存储.
 *
 * @author devil
 * @date Created in 2022/4/26 9:34
 */
public class LocalFileStorageServiceImpl implements StorageService<String> {
    
    /**
     * 根路径.
     */
    private String root;
    
    public LocalFileStorageServiceImpl() {
        this.root = ".";
    }
    
    public LocalFileStorageServiceImpl(String root) {
        this.root = root;
    }
    
    /**
     * root : the root to set.
     */
    public void setRoot(String root) {
        this.root = root;
    }
    
    /**
     * 暂时是覆盖  存在相同名的文件覆盖.
     */
    @Override
    public LocalFileStorableObject create(String path, int permission) throws StorageException {
        if (isExist(path)) {
            throw new StorageException(StorageErrorCode.FILE_EXIST);
        }
        LocalFileStorableObject result = new LocalFileStorableObject(root, path);
        try {
            File file = result.getFile();
            File parent = file.getParentFile();
            file.setWritable(StorablePermission.isWritable(permission));
            file.setReadable(StorablePermission.isReadable(permission));
            if (!parent.exists()) {
                parent.mkdirs();
            }
            result.getFile().createNewFile();
        } catch (IOException e) {
            throw new StorageException(StorageErrorCode.LOCAL_CREATE_FILE_FAIL, e);
        }
        return result;
    }
    
    @Override
    public LocalFileStorableObject get(String path) throws StorageException {
        LocalFileStorableObject result = new LocalFileStorableObject(root, path);
        isFileExist(result.getFile());
        return result;
    }
    
    @Override
    public String getFullAccessPath(String path) throws StorageException {
        return root + path;
    }
    
    @Override
    public LocalFileStorableObject put(String path, StorableObject source, String metaData) throws StorageException {
        return put(path, source, source.getPermission());
    }
    
    @Override
    public LocalFileStorableObject put(String path, StorableObject source, int permission) throws StorageException {
        LocalFileStorableObject target = new LocalFileStorableObject(root, path, permission);
        try {
            if (!isExist(path)) {
                throw new StorageException(StorageErrorCode.FILE_NOT_EXIST);
            }
            BufferedInputStream in = new BufferedInputStream(source.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(target.getOutputStream());
            byte[] buff = new byte[1024];
            int b;
            while ((b = in.read(buff, 0, buff.length)) > 0) {
                out.write(buff, 0, b);
            }
            in.close();
            out.close();
            return target;
        } catch (IOException e) {
            throw new StorageException(StorageErrorCode.LOCAL_WRITE_FILE_FAIL, e);
        }
    }
    
    @Override
    public void remove(String path) throws StorageException {
        File file = new File(root, path);
        isFileExist(file);
        if (!file.delete()) {
            throw new StorageException(StorageErrorCode.LOCAL_DELETE_FILE_FAIL, "删除本地文件失败 文件路径：'" + path + "'");
        }
    }
    
    @Override
    public void setPermission(String path, int permission) throws StorageException {
        File file = new File(root, path);
        isFileExist(file);
        file.setReadable(StorablePermission.isReadable(permission));
        file.setWritable(StorablePermission.isWritable(permission));
    }
    
    @Override
    public boolean isExist(String path) throws StorageException {
        File file = new File(root, path);
        return file.exists() && file.isFile();
    }

    private boolean isFileExist(File file) {
        if (file == null) {
            return false;
        }
        if (!(file.exists() && file.isFile())) {
            throw new StorageException(StorageErrorCode.FILE_NOT_EXIST);
        }
        return true;
    }
    
}
