package com.devil.fission.machine.object.storage.alibaba;

import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectAcl;
import com.aliyun.oss.model.ObjectPermission;
import com.devil.fission.machine.object.storage.core.StorableObject;
import com.devil.fission.machine.object.storage.core.StorablePermission;

import java.io.IOException;
import java.io.InputStream;

/**
 * 阿里oss存储对象.
 *
 * @author devil
 * @date Created in 2023/3/21 16:03
 */
public class OssStorableObject implements StorableObject {
    
    private OSSObject ossObject;
    
    private OssStorageService storageService;
    
    private String key;
    
    public OssStorableObject() {
    }
    
    public OssStorableObject(OssStorageService storageService, String key) {
        this.storageService = storageService;
        this.key = key;
        this.ossObject = storageService.ossClient.getObject(storageService.bucket, key);
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return ossObject.getObjectContent();
    }
    
    @Override
    public String getKey() {
        return key;
    }
    
    @Override
    public int getPermission() {
        ObjectAcl acl = storageService.ossClient.getObjectAcl(storageService.bucket, key);
        ObjectPermission objectPermission = acl.getPermission();
        if (objectPermission == ObjectPermission.Default) {
            return getPermission(storageService.ossClient.getBucketAcl(storageService.bucket).getCannedACL());
        }
        return getPermission(objectPermission);
    }
    
    protected static int getPermission(CannedAccessControlList cannedAccessControlList) {
        switch (cannedAccessControlList) {
            case PublicRead:
                return StorablePermission.READABLE;
            case PublicReadWrite:
                return StorablePermission.PUBLIC;
            case Default:
            case Private:
            default:
                return StorablePermission.PRIVATE;
        }
    }
    
    protected static int getPermission(ObjectPermission objectPermission) {
        switch (objectPermission) {
            case PublicRead:
                return StorablePermission.READABLE;
            case PublicReadWrite:
                return StorablePermission.PUBLIC;
            case Default:
            case Private:
            default:
                return StorablePermission.PRIVATE;
        }
    }
}
