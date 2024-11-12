package com.fission.machine.generator.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期处理.
 *
 * @author devil
 * @date Created in 2022/4/27 10:15
 */
public class DateUtils {
    
    /**
     * 时间格式(yyyy-MM-dd).
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    
    /**
     * 时间格式(yyyy-MM-dd HH:mm:ss).
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * Date 转 String.
     *
     * @param date Date
     * @return String
     */
    public static String format(Date date) {
        return format(date, DATE_PATTERN);
    }
    
    /**
     * Date 转 String.
     *
     * @param date   Date
     * @param pattern 时间格式
     * @return String
     */
    public static String format(Date date, String pattern) {
        if (date != null) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }
}
