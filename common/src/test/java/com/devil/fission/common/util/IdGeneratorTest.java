 package com.devil.fission.common.util;
 
 import org.junit.Assert;
 import org.junit.Test;

 /**
  * {@link IdGenerator } unit test.
  *
  * @author Devil
  * @date Created in 2023/3/9 17:41
  */
 public class IdGeneratorTest {

     @Test
     public void testGeneratorId() {
         long nextId = IdGeneratorEnum.INSTANCE.getIdGenerator().nextId();
         System.out.println(nextId);
         Assert.assertTrue(nextId > 0);
     }

 }