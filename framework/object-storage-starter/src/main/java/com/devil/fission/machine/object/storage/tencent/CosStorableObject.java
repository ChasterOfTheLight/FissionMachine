package com.devil.fission.machine.object.storage.tencent;

import com.qcloud.cos.model.AccessControlList;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.CannedAccessControlList;
import com.devil.fission.machine.object.storage.core.StorableObject;
import com.devil.fission.machine.object.storage.core.StorablePermission;

import java.io.IOException;
import java.io.InputStream;

/**
 * 腾讯云cos存储对象.
 *
 * @author devil
 * @date Created in 2022/4/26 11:13
 */
public class CosStorableObject implements StorableObject {
    
    private COSObject cosObject;
    
    private CosStorageService storageService;
    
    private String key;
    
    public CosStorableObject() {
    }
    
    public CosStorableObject(CosStorageService storageService, String key) {
        this.storageService = storageService;
        this.key = key;
        this.cosObject = storageService.cosClient.getObject(storageService.bucket, key);
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return cosObject.getObjectContent();
    }
    
    @Override
    public String getKey() {
        return key;
    }
    
    @Override
    public int getPermission() {
        AccessControlList acl = storageService.cosClient.getObjectAcl(storageService.bucket, key);
        CannedAccessControlList cannedAccessControlList = acl.getCannedAccessControl();
        if (cannedAccessControlList == CannedAccessControlList.Default) {
            cannedAccessControlList = storageService.cosClient.getBucketAcl(storageService.bucket).getCannedAccessControl();
        }
        return getPermission(cannedAccessControlList);
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
}
