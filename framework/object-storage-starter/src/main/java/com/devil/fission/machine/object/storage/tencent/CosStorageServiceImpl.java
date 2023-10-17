package com.devil.fission.machine.object.storage.tencent;

import com.devil.fission.machine.object.storage.core.StorableObject;
import com.devil.fission.machine.object.storage.core.StorablePermission;
import com.devil.fission.machine.object.storage.core.StorableRequest;
import com.devil.fission.machine.object.storage.core.StorageErrorCode;
import com.devil.fission.machine.object.storage.core.StorageException;
import com.devil.fission.machine.object.storage.core.StorageService;
import com.devil.fission.machine.object.storage.core.TempSecret;
import com.devil.fission.machine.object.storage.util.FileMd5Util;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.Bucket;
import com.qcloud.cos.model.CannedAccessControlList;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;
import org.apache.http.impl.io.EmptyInputStream;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

/**
 * 腾讯云cos对象存储器.
 *
 * @author devil
 * @date Created in 2022/4/26 11:15
 */
public class CosStorageServiceImpl implements StorageService<ObjectMetadata>, TempSecret<CosTempSecretObject> {
    
    final COSClient cosClient;
    
    private final CosProperties properties;
    
    String bucket;
    
    public CosStorageServiceImpl(COSClient cosClient, CosProperties properties) {
        this.cosClient = cosClient;
        this.properties = properties;
        this.bucket = properties.getBucket();
        if (bucket == null) {
            List<Bucket> buckets = cosClient.listBuckets();
            if (buckets != null && buckets.size() > 0) {
                this.bucket = buckets.get(0).getName();
            }
        }
    }
    
    @Override
    public CosStorableObject create(String path, int permission) {
        PutObjectRequest req = new PutObjectRequest(bucket, path, EmptyInputStream.INSTANCE, null);
        req.withCannedAcl(getAcl(permission));
        try {
            cosClient.putObject(req);
            return new CosStorableObject(this, path);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.COS_CREATE_FILE_FAIL, e);
        }
    }
    
    @Override
    public CosStorableObject get(String path) {
        try {
            return new CosStorableObject(this, path);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.COS_GET_FILE_FAIL, e);
        }
    }
    
    @Override
    public String getFullAccessPath(String path) throws StorageException {
        String region = properties.getRegion();
        // host的格式为 https://<Bucket>.cos.<Region>.myqcloud.com/
        String host = "https://" + bucket + ".cos." + region + ".myqcloud.com";
        return host + path;
    }
    
    @Override
    public CosStorableObject put(String path, StorableObject source, ObjectMetadata metaData) throws StorageException {
        try {
            StorableRequest request = FileMd5Util.getStorableRequest(source.getInputStream(), path);
            String objectName = request.getObjectName();
            if (!isExist(objectName)) {
                PutObjectRequest req = new PutObjectRequest(bucket, objectName, request.getInputStream(), null);
                if (metaData != null) {
                    req.setMetadata(metaData);
                }
                req.withCannedAcl(getAcl(source.getPermission()));
                cosClient.putObject(req);
            }
            return new CosStorableObject(this, objectName);
        } catch (IOException e) {
            throw new StorageException(StorageErrorCode.COS_PUT_FILE_FAIL, e);
        }
    }
    
    @Override
    public CosStorableObject put(String path, StorableObject source, int permission) throws StorageException {
        try {
            StorableRequest request = FileMd5Util.getStorableRequest(source.getInputStream(), path);
            String objectName = request.getObjectName();
            if (!isExist(objectName)) {
                PutObjectRequest req = new PutObjectRequest(bucket, objectName, request.getInputStream(), null);
                req.withCannedAcl(getAcl(permission));
                cosClient.putObject(req);
            }
            return new CosStorableObject(this, objectName);
        } catch (IOException e) {
            throw new StorageException(StorageErrorCode.COS_PUT_FILE_FAIL, e);
        }
    }
    
    @Override
    public void remove(String path) {
        try {
            cosClient.deleteObject(bucket, path);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.COS_DELETE_FILE_FAIL, e);
        }
    }
    
    @Override
    public void setPermission(String path, int permission) {
        try {
            cosClient.setObjectAcl(bucket, path, getAcl(permission));
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.COS_SET_PERMISSION_FAIL, e);
        }
    }
    
    @Override
    public boolean isExist(String path) {
        try {
            return cosClient.doesObjectExist(bucket, path);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.COS_FILE_IS_EXIST_FAIL, e);
        }
    }
    
    /**
     * 获取cos权限.
     */
    protected static CannedAccessControlList getAcl(int permission) {
        switch (permission) {
            case StorablePermission.PUBLIC:
            case StorablePermission.WRITABLE:
                return CannedAccessControlList.PublicReadWrite;
            case StorablePermission.PRIVATE:
                return CannedAccessControlList.Private;
            case StorablePermission.READABLE:
                return CannedAccessControlList.PublicRead;
            default:
                return CannedAccessControlList.Default;
        }
    }
    
    @Override
    public CosTempSecretObject generateUploadTempSecret(String path) {
        TreeMap<String, Object> config = new TreeMap<>();
        
        try {
            // 替换为您的 SecretId
            config.put("SecretId", properties.getSecretId());
            // 替换为您的 SecretKey
            config.put("SecretKey", properties.getSecretKey());
            // 临时密钥有效时长，单位是秒，默认1800秒，目前主账号最长2小时（即7200秒），子账号最长36小时（即129600秒）
            config.put("durationSeconds", 2 * 60 * 60);
            // 换成您的 bucket
            config.put("bucket", properties.getBucket());
            // 换成 bucket 所在地区
            config.put("region", properties.getRegion());
            // 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径，例子：a.jpg 或者 a/* 或者 * 。
            // 如果填写了“*”，将允许用户访问所有资源；除非业务需要，否则请按照最小权限原则授予用户相应的访问权限范围。
            config.put("allowPrefix", path);
            
            // 密钥的权限列表。简单上传、表单上传和分片上传需要以下的权限，其他权限列表请看 https://cloud.tencent.com/document/product/436/31923
            String[] allowActions = new String[] {
                    // 简单上传
                    "name/cos:PutObject", "name/cos:PostObject", "name/cos:InitiateMultipartUpload", "name/cos:ListMultipartUploads",
                    "name/cos:ListParts", "name/cos:UploadPart", "name/cos:CompleteMultipartUpload"};
            config.put("allowActions", allowActions);
            
            Response response = CosStsClient.getCredential(config);
            
            CosTempSecretObject tempSecretObject = new CosTempSecretObject();
            tempSecretObject.setTmpSecretId(response.credentials.tmpSecretId);
            tempSecretObject.setTmpSecretKey(response.credentials.tmpSecretKey);
            tempSecretObject.setSessionToken(response.credentials.sessionToken);
            tempSecretObject.setStartTime(String.valueOf(response.startTime));
            tempSecretObject.setExpiredTime(String.valueOf(response.expiredTime));
            return tempSecretObject;
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.COS_TEMP_SECRET_FAIL, e);
        }
    }
}
