package com.devil.fission.machine.object.storage.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link StorablePermission } unit test.
 *
 * @author Devil
 * @date Created in 2023/3/21 16:20
 */
public class StorablePermissionTest {
    
    @Test
    public void testIsReadable() {
        boolean readable = StorablePermission.isReadable(1);
        Assert.assertTrue(readable);
    }
    
    @Test
    public void testIsWritable() {
        boolean writable = StorablePermission.isWritable(0);
        Assert.assertFalse(writable);
    }
    
    @Test
    public void testCompute() {
        int compute = StorablePermission.compute(true, true);
        Assert.assertEquals(StorablePermission.READABLE | StorablePermission.WRITABLE, compute);
    }
}