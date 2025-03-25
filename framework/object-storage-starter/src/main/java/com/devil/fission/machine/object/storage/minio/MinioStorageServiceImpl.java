package com.devil.fission.machine.object.storage.minio;

import com.devil.fission.machine.object.storage.core.StorableObject;
import com.devil.fission.machine.object.storage.core.StorageErrorCode;
import com.devil.fission.machine.object.storage.core.StorageException;
import com.devil.fission.machine.object.storage.core.StorageService;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * minio存储服务实现.
 *
 * @author Devil
 * @date Created in 2024/10/25 13:59
 */
@Slf4j
public class MinioStorageServiceImpl implements StorageService<String> {
    
    private final MinioClient minioClient;
    
    private final MinioProperties minioProperties;
    
    String bucket;
    
    public MinioStorageServiceImpl(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
        bucket = minioProperties.getBucket();
    }
    
    @Override
    public StorableObject create(String path, int permission) throws StorageException {
        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(path).stream(new ByteArrayInputStream(new byte[] {}), 0, -1).build());
            return new MinioStorableObject(path, null);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_CREATE_FILE_FAIL, e);
        }
    }
    
    @Override
    public StorableObject get(String path) throws StorageException {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(path).build())) {
            return new MinioStorableObject(path, stream);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_GET_FILE_FAIL, e);
        }
    }
    
    @Override
    public String getFullAccessPath(String path) throws StorageException {
        String endpoint = minioProperties.getEndpoint();
        return "http://" + bucket + "." + endpoint + "/" + path;
    }
    
    @Override
    public StorableObject put(String path, StorableObject source, String metaData) throws StorageException {
        return null;
    }
    
    @Override
    public StorableObject put(String path, StorableObject source, int permission) throws StorageException {
        InputStream sourceInputStream;
        try {
            sourceInputStream = source.getInputStream();
            if (!isExist(path)) {
                minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(path).stream(sourceInputStream, -1, 10485760).build());
            }
            return new MinioStorableObject(path, sourceInputStream);
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_PUT_FILE_FAIL, e);
        }
    }
    
    @Override
    public void remove(String path) throws StorageException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(path).build());
        } catch (Exception e) {
            throw new StorageException(StorageErrorCode.OSS_DELETE_FILE_FAIL, e);
        }
    }
    
    @Override
    public boolean isExist(String path) throws StorageException {
        try {
            GetObjectResponse objectResponse = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(path).build());
            if (objectResponse != null && objectResponse.object() != null) {
                return true;
            }
        } catch (Exception e) {
            log.warn(e.getLocalizedMessage(), e);
        }
        return false;
    }
}
