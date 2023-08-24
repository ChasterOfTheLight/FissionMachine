package com.devil.fission.machine.common.util;

import cn.hutool.core.lang.RegexPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具类.
 *
 * @author devil
 * @date Created in 2022/12/6 9:55
 */
public class RegexUtils {
    
    private static final int MAX_LEN = 256;
    
    /**
     * 判断字符串是否符合正则表达式.
     */
    public static boolean find(String str, String regex) {
        if (str == null || str.isEmpty() || str.length() > MAX_LEN || regex == null || regex.isEmpty() || regex.length() > MAX_LEN) {
            return false;
        }
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.find();
    }
    
    /**
     * 判断输入的字符串是否符合Email格式.
     */
    public static boolean isEmail(String email) {
        if (email == null || email.isEmpty() || email.length() > MAX_LEN) {
            return false;
        }
        Pattern pattern = Pattern.compile(RegexPool.EMAIL, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(email).matches();
    }
    
    /**
     * 判断是否为url.
     */
    public static boolean isUrl(String value) {
        if (value == null || value.isEmpty() || value.length() > MAX_LEN) {
            return false;
        }
        Pattern pattern = Pattern.compile(RegexPool.URL);
        return pattern.matcher(value).matches();
    }
    
    /**
     * 判断是否为手机号.
     */
    public static boolean isMobile(String value) {
        if (value == null || value.isEmpty() || value.length() > MAX_LEN) {
            return false;
        }
        Pattern pattern = Pattern.compile(RegexPool.MOBILE);
        return pattern.matcher(value).matches();
    }
    
    /**
     * 判断是否为浮点数，包括double和float.
     */
    public static boolean isDouble(String value) {
        if (value == null || value.isEmpty() || value.length() > MAX_LEN) {
            return false;
        }
        String regex = "^[-\\+]?\\d+\\.\\d+$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(value).matches();
    }
    
    /**
     * 判断是否为整数.
     */
    public static boolean isInteger(String value) {
        if (value == null || value.isEmpty() || value.length() > MAX_LEN) {
            return false;
        }
        String regex = "^[-\\+]?[\\d]+$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(value).matches();
    }
    
    /**
     * 替换消息模版.
     */
    public static String replaceMessageTemplate(String messageTemplate, Map<String, String> replaceMap) {
        if (StringUtils.isEmpty(messageTemplate) || replaceMap == null || replaceMap.isEmpty()) {
            return messageTemplate;
        }
        String regex = "\\$\\{.+?\\}";
        // 根据正则查询占位
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(messageTemplate);
        List<String> matchList = new ArrayList<>();
        while (m.find()) {
            matchList.add(m.group());
        }
        if (matchList.isEmpty()) {
            return messageTemplate;
        }
        String messageContent = messageTemplate;
        // 遍历占位集合替换
        for (String placeHolder : matchList) {
            String key = placeHolder.replace("${", "").replace("}", "");
            String value = replaceMap.get(key);
            if (value != null) {
                messageContent = messageContent.replace(placeHolder, value);
            }
        }
        return messageContent;
    }
    
}