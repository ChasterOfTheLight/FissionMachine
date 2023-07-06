 package com.devil.fission.common.util;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;

 /**
  * {@link CollectionUtils } unit test.
  *
  * @author Devil
  * @date Created in 2023/3/8 14:05
  */
 public class CollectionUtilsTest {

     @Test
     public void testIsEmpty() {
         List<String> list = Collections.emptyList();
         Assert.assertTrue(CollectionUtils.isEmpty(list));
     }

     @Test
     public void testIsNotEmpty() {
         List<String> list = Collections.singletonList("123");
         Assert.assertTrue(CollectionUtils.isNotEmpty(list));
     }

     @Test
     public void testContainsSub() {
         Set<Set<String>> setA = new HashSet<>();
         Set<String> setB = new HashSet<>(Arrays.asList("1,2", "3, 4", "5"));
         setA.add(setB);
         Set<String> setC = new HashSet<>(Arrays.asList("1,2", "3, 4", "5", "6", "7", "8"));
         Assert.assertTrue(CollectionUtils.containsSub(setA, setC));
     }
 }