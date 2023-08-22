package com.devil.fission.machine.object.storage.alibaba;

import com.devil.fission.machine.object.storage.core.StorableObject;
import com.devil.fission.machine.object.storage.core.StorablePermission;
import com.devil.fission.machine.object.storage.local.LocalFileStorableObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * {@link OssStorageServiceImpl } unit test.
 *
 * @author Devil
 * @date Created in 2023/3/22 13:45
 */
@RunWith(MockitoJUnitRunner.class)
public class OssStorageServiceImplTest {
    
    // 实际使用中可以用注入方式
    @Mock
    private OssStorageServiceImpl ossStorageServiceImpl;
    
    @Test
    public void ossStorageTest() {
        String path = "/tmp/test.txt";
        String fileName = "123";
        // 从本地文件上传到远端
        LocalFileStorableObject storableObject = new LocalFileStorableObject(path, fileName);
        Mockito.when(ossStorageServiceImpl.put(path, storableObject, StorablePermission.PUBLIC)).thenReturn(new OssStorableObject());
        StorableObject object = ossStorageServiceImpl.put(path, storableObject, StorablePermission.PUBLIC);
        Assert.assertNotNull(object);
        
        Mockito.when(ossStorageServiceImpl.isExist(path)).thenReturn(true);
        boolean exist = ossStorageServiceImpl.isExist(path);
        Assert.assertTrue(exist);
    }
    
}