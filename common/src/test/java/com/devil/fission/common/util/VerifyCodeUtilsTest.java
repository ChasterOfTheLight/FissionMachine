 package com.devil.fission.common.util;
 
 import org.junit.Assert;
 import org.junit.Test;

 /**
  * {@link VerifyCodeUtils } unit test.
  *
  * @author Devil
  * @date Created in 2023/3/13 10:55
  */
 public class VerifyCodeUtilsTest {

     @Test
     public void testGetCode() {
         String code = VerifyCodeUtils.getCode(6);
         System.out.println(code);
         Assert.assertNotNull(code);
     }
 }