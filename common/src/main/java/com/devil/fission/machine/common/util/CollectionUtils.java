package com.devil.fission.machine.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 集合工具类.
 *
 * @author Devil
 * @date Created in 2023/3/8 9:29
 */
public class CollectionUtils {
    
    private CollectionUtils() {
        throw new UnsupportedOperationException();
    }
    
    public static <T> boolean isEmpty(Collection<T> list) {
        return list == null || list.isEmpty();
    }
    
    public static <T> boolean isNotEmpty(Collection<T> list) {
        return !isEmpty(list);
    }
    
    public static <T> Set<T> newSet(T... vals) {
        return ofSet(vals);
    }
    
    public static <T> Set<T> ofSet(T... vals) {
        HashSet<T> set = new HashSet<>();
        if (vals instanceof String[]) {
            String[] strings = (String[]) vals;
            for (String val : strings) {
                if (val != null) {
                    set.add((T) val.trim());
                }
            }
        } else {
            set.addAll(Arrays.asList(vals));
        }
        return set;
    }
    
    public static <T> List<T> ofList(T... vals) {
        ArrayList<T> list = new ArrayList<>();
        if (vals instanceof String[]) {
            String[] strings = (String[]) vals;
            for (String val : strings) {
                if (val != null) {
                    list.add((T) val.trim());
                }
            }
        } else {
            list.addAll(Arrays.asList(vals));
        }
        return list;
    }
    
    /**
     * 解析一个单元素set，获得其中的一个元素.
     *
     * @param set set
     * @return one
     */
    public static String resolveSingletonSet(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        return set.iterator().next();
    }
    
    /**
     * 获得单元素set.
     *
     * @param val val
     * @return 单元素set
     */
    public static Set<String> singletonSet(String val) {
        return new HashSet<>(Collections.singletonList(val));
    }
    
    /**
     * 将字符串数组转换为set集合.
     *
     * @param separator 字符串分割符号
     * @param value     value
     * @return 一个经过去重的集合列表，该集合元素是去重集合
     */
    public static Set<Set<String>> splitStrValsToSets(String separator, String... value) {
        Set<Set<String>> ret = new HashSet<>();
        for (String val : newSet(value)) {
            ret.add(newSet(val.split(separator)));
        }
        return ret.size() > 0 ? ret : null;
    }
    
    /**
     * 判断目标集合包含否是源集合的子集或者源集合的任意一项的子集.
     *
     * @param sets 源集合
     * @param list 目标集合
     * @param <T>  此集合中元素的类型
     */
    public static <T> boolean containsSub(Set<Set<T>> sets, List<T> list) {
        if (list == null) {
            return false;
        }
        return containsSub(sets, new HashSet<>(list));
    }
    
    /**
     * 判断目标集合是否包含源集合的子集或者源集合的任意一项的子集.
     *
     * @param sets       源集合
     * @param collection 目标集合
     * @param <T>        此集合中元素的类型
     */
    public static <T> boolean containsSub(Set<Set<T>> sets, Collection<T> collection) {
        if (collection == null) {
            return false;
        }
        if (collection instanceof Set) {
            return containsSub(sets, (Set<T>) collection);
        } else {
            return containsSub(sets, new HashSet<>(collection));
        }
    }
    
    /**
     * 判断目标集合是否包含源集合的子集或者源集合的任意一项的子集.
     *
     * @param sets 源集合
     * @param set  目标集合
     * @param <T>  此集合中元素的类型
     */
    public static <T> boolean containsSub(Set<Set<T>> sets, Set<T> set) {
        if (set == null) {
            return sets == null;
        }
        if (CollectionUtils.isEmpty(sets)) {
            return false;
        }
        return sets.stream().anyMatch(set::containsAll);
    }
    
}
