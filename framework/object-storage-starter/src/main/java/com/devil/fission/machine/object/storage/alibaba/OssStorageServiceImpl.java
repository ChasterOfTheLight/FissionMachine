package com.devil.fission.machine.object.storage.alibaba;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.*;
import com.devil.fission.machine.object.storage.core.StorableObject;
import com.devil.fission.machine.object.storage.core.StorablePermission;
import com.devil.fission.machine.object.storage.core.StorableRequest;
import com.devil.fission.machine.object.storage.core.StorageErrorCode;
import com.devil.fission.machine.object.storage.core.StorageException;
import com.devil.fission.machine.object.storage.core.StorageService;
import com.devil.fission.machine.object.storage.core.TempSecret;
import com.devil.fission.machine.object.storage.util.FileMd5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.io.EmptyInputStream;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 阿里云对象储存器.
 *
 * @author devil
 * @date Created in 2023/8/22 17:24
 */
@Slf4j
public class OssStorageServiceImpl implements StorageService<ObjectMetadata>, TempSecret<OssTempSecretObject> {

    final OSS ossClient;

    private final OssProperties properties;

    String bucket;

    public OssStorageServiceImpl(OSS ossClient, OssProperties properties) {
        this.ossClient = ossClient;
        this.properties = properties;
        this.bucket = properties.getBucket();
        if (bucket == null) {
            List<Bucket> buckets = ossClient.listBuckets();
            if (buckets != null && buckets.size() > 0) {
                this.bucket = buckets.get(0).getName();
            }
        }
    }

    @Override
    public OssStorableObject create(String path, int permission) throws StorageException {
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setObjectAcl(getAcl(permission));
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, path, EmptyInputStream.INSTANCE, objectMetadata);
            ossClient.putObject(putObjectRequest);
            return new OssStorableObject(this, path);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_CREATE_FILE_FAIL, e);
        }
    }

    @Override
    public OssStorableObject get(String path) throws StorageException {
        try {
            return new OssStorableObject(this, path);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_GET_FILE_FAIL, e);
        }
    }
    
    @Override
    public String getFullAccessPath(String path) throws StorageException {
        String endpoint = properties.getEndpoint();
        // host的格式为 bucketname.endpoint
        return "https://" + bucket + "." + endpoint + "/" + path;
    }
    
    @Override
    public OssStorableObject put(String path, StorableObject source, ObjectMetadata metadata) throws StorageException {
        try {
            StorableRequest request = FileMd5Util.getStorableRequest(source.getInputStream(), path);
            String objectName = request.getObjectName();
            if (!isExist(objectName)) {
                PutObjectRequest req = new PutObjectRequest(bucket, objectName, request.getInputStream(), metadata);
                if (metadata == null) {
                    metadata = new ObjectMetadata();
                }
                metadata.setObjectAcl(getAcl(source.getPermission()));
                ossClient.putObject(req);
            }
            return new OssStorableObject(this, objectName);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_PUT_FILE_FAIL, e);
        }
    }

    @Override
    public OssStorableObject put(String path, StorableObject source, int permission) throws StorageException {
        try {
            StorableRequest request = FileMd5Util.getStorableRequest(source.getInputStream(), path);
            String objectName = request.getObjectName();
            if (!isExist(objectName)) {
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setObjectAcl(getAcl(permission));
                PutObjectRequest req = new PutObjectRequest(bucket, objectName, request.getInputStream(), objectMetadata);
                ossClient.putObject(req);
            }
            return new OssStorableObject(this, objectName);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_PUT_FILE_FAIL, e);
        }
    }

    @Override
    public void remove(String path) throws StorageException {
        try {
            ossClient.deleteObject(bucket, path);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_DELETE_FILE_FAIL, e);
        }
    }

    @Override
    public void setPermission(String path, int permission) throws StorageException {
        try {
            ossClient.setObjectAcl(bucket, path, getAcl(permission));
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_SET_PERMISSION_FAIL, e);
        }
    }

    @Override
    public boolean isExist(String path) throws StorageException {
        try {
            return ossClient.doesObjectExist(bucket, path);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_FILE_IS_EXIST_FAIL, e);
        }
    }

    /**
     * 获取oss权限.
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
    public OssTempSecretObject generateUploadTempSecret(String path) {
        String accessId = properties.getAccessKeyId();
        String endpoint = properties.getEndpoint();
        // host的格式为 bucketname.endpoint
        String host = "https://" + bucket + "." + endpoint;

        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, path);

            // 授权post权限
            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            OssTempSecretObject tempSecretObject = new OssTempSecretObject();
            tempSecretObject.setAccessId(accessId);
            tempSecretObject.setPolicy(encodedPolicy);
            tempSecretObject.setSignature(postSignature);
            tempSecretObject.setDir(path);
            tempSecretObject.setHost(host);
            // 毫秒 -> 秒
            tempSecretObject.setExpire(String.valueOf(expireEndTime / 1000));
            return tempSecretObject;
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_TEMP_SECRET_FAIL, e);
        }
    }
}
