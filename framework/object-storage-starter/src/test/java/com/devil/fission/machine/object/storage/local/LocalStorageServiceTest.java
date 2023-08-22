package com.devil.fission.machine.object.storage.local;

import com.devil.fission.machine.object.storage.core.StorablePermission;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@link LocalFileStorageServiceImpl } unit test.
 *
 * @author Devil
 * @date Created in 2023/3/22 13:39
 */
public class LocalStorageServiceTest {
    
    LocalFileStorageServiceImpl storageService = new LocalFileStorageServiceImpl("/root");
    
    @Test
    public void localStorageTest() {
        String path = "/tmp/test.txt";
        storageService.create(path, StorablePermission.PUBLIC);
        LocalFileStorableObject storableObject = storageService.get(path);
        Assert.assertNotNull(storableObject.getFile());
        
        boolean exist = storageService.isExist(path);
        Assert.assertTrue(exist);
        
        storageService.remove(path);
        exist = storageService.isExist(path);
        Assert.assertFalse(exist);
    }
}