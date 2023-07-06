 package com.devil.fission.common.support;
 
 import org.junit.Assert;
 import org.junit.Test;

 /**
  * {@link SqlFilter } unit test.
  *
  * @author Devil
  * @date Created in 2023/3/8 14:48
  */
 public class SqlFilterTest {

     @Test
     public void testSqlInject() {
         String s = SqlFilter.sqlInject("1234566");
         Assert.assertEquals(s, "1234566");

         s = SqlFilter.sqlInject("1234566\"23;45");
         Assert.assertEquals(s, "12345662345");
     }
 }