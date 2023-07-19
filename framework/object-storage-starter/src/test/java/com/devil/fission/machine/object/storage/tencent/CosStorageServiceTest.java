package com.devil.fission.machine.object.storage.tencent;

import com.devil.fission.machine.object.storage.core.StorableObject;
import com.devil.fission.machine.object.storage.core.StorablePermission;
import com.devil.fission.machine.object.storage.local.InputStreamStorableObject;
import com.devil.fission.machine.object.storage.local.LocalFileStorableObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * {@link CosStorageService } unit test.
 *
 * @author Devil
 * @date Created in 2023/3/22 14:16
 */
@RunWith(MockitoJUnitRunner.class)
public class CosStorageServiceTest {
    
    // 实际使用中可以用注入方式
    @Mock
    private CosStorageService cosStorageService;
    
    @Test
    public void cosStorageTest() throws IOException {
        String path = "/tmp/test.txt";
        String fileName = "123";
        // 从本地文件上传到远端
        LocalFileStorableObject storableObject = new LocalFileStorableObject(path, fileName);
        Mockito.when(cosStorageService.put(path, storableObject, StorablePermission.PUBLIC)).thenReturn(new CosStorableObject());
        StorableObject object = cosStorageService.put(path, storableObject, StorablePermission.PUBLIC);
        Assert.assertNotNull(object);
        
        // 从流上传
        InputStreamStorableObject storableObject2 = new InputStreamStorableObject(Files.newInputStream(new File("123").toPath()));
        Mockito.when(cosStorageService.put(path, storableObject2, StorablePermission.PUBLIC)).thenReturn(new CosStorableObject());
        StorableObject object2 = cosStorageService.put(path, storableObject2, StorablePermission.PUBLIC);
        Assert.assertNotNull(object2);
        
        Mockito.when(cosStorageService.isExist(path)).thenReturn(true);
        boolean exist = cosStorageService.isExist(path);
        Assert.assertTrue(exist);
    }
    
}