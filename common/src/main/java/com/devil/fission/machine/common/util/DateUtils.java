package com.devil.fission.machine.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 日期工具类.
 *
 * @author Devil
 * @date Created in 2022/12/5 15:46
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
     * UTC时间格式(yyyy-MM-dd'T'HH:mm:ss.SSS).
     */
    public static final String UTC_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    
    /**
     * 没有时间分隔符的时间格式(yyyyMMddHHmmss).
     */
    public static final String DATE_TIME_PATTERN_NON_SPLIT = "yyyyMMddHHmmss";
    
    /**
     * 日志格式化时多中类型判断格式化.
     */
    private static final String[] PARSE_PATTERNS = {DATE_PATTERN, DATE_TIME_PATTERN};
    
    /**
     * fast date日期格式化(yyyy-MM-dd).
     */
    public static final FastDateFormat FAST_DATE_FORMAT = FastDateFormat.getInstance(DATE_PATTERN, TimeZone.getTimeZone("GMT+8"));
    
    /**
     * fast date时间格式化(yyyy-MM-dd HH:mm:ss).
     */
    public static final FastDateFormat FAST_DATE_TIME_FORMAT = FastDateFormat.getInstance(DATE_TIME_PATTERN, TimeZone.getTimeZone("GMT+8"));
    
    /**
     * fast date utc时间格式化(yyyy-MM-dd'T'HH:mm:ss.SSS).
     */
    public static final FastDateFormat FAST_UTC_DATE_TIME_FORMAT = FastDateFormat.getInstance(UTC_DATE_TIME_PATTERN, TimeZone.getTimeZone("GMT+8"));
    
    /**
     * fast date 没有时间分隔符的时间格式(yyyyMMddHHmmss).
     */
    public static final FastDateFormat FAST_DATE_TIME_PATTERN_NON_SPLIT = FastDateFormat.getInstance(DATE_TIME_PATTERN_NON_SPLIT,
            TimeZone.getTimeZone("GMT+8"));
    
    /**
     * 获取当前时间（date）.
     */
    public static Date now() {
        return Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 获取当前时间（long）.
     */
    public static long nowTime() {
        return now().getTime();
    }
    
    /**
     * 格式化日期yyyy-MM-dd.
     */
    public static String fastDateFormat(Date date) {
        if (date == null) {
            return null;
        }
        return FAST_DATE_FORMAT.format(date);
    }
    
    /**
     * 格式化日期yyyy-MM-dd HH:mm:ss.
     */
    public static String fastDateTimeFormat(Date date) {
        if (date == null) {
            return null;
        }
        return FAST_DATE_TIME_FORMAT.format(date);
    }
    
    /**
     * 格式化日期yyyy-MM-dd'T'HH:mm:ss.SSS.
     */
    public static String fastUtcDateTimeFormat(Date date) {
        if (date == null) {
            return null;
        }
        return FAST_UTC_DATE_TIME_FORMAT.format(date);
    }
    
    /**
     * 格式化日期yyyyMMddHHmmss.
     */
    public static String fastDateTimePatternNonSplitFormat(Date date) {
        if (date == null) {
            return null;
        }
        return FAST_DATE_TIME_PATTERN_NON_SPLIT.format(date);
    }
    
    /**
     * 字符串转换成日期.
     *
     * @param strDate 日期字符串
     * @param pattern 日期的格式，如：yyyy-MM-dd
     */
    public static Date stringToDate(String strDate, String pattern) {
        if (StringUtils.isBlank(strDate)) {
            return null;
        }
        
        // 使用DateTimeFormatter，因为该类有个格式化缓存，如果是相同的格式化，会减少formatter的创建
        DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
        return fmt.parseLocalDateTime(strDate).toDate();
    }
    
    /**
     * 计算日期差.
     *
     * @param before 前值日期
     * @param after  后前日期
     * @return 后值-前值
     */
    public static Duration duration(Date before, Date after) {
        Instant instantBefore = before.toInstant();
        Instant instantNow = after.toInstant();
        return Duration.between(instantBefore, instantNow);
    }
    
    /**
     * 获取今日23：59：59时间.
     */
    public static Date getTodayLastSecondDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        return calendar.getTime();
    }
    
    /**
     * 获取 N天前后的   00：00：00.
     */
    public static Date getLastSomeDayDate(int beforeDays, boolean isLastSecond) {
        return getLastSomeDayDate(new Date(), beforeDays, isLastSecond);
    }
    
    /**
     * 获取 date天前后的   00：00：00.
     */
    public static Date getLastSomeDayDate(Date date, int beforeDays, boolean isLastSecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -beforeDays);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        if (isLastSecond) {
            calendar.set(Calendar.SECOND, -1);
        } else {
            calendar.set(Calendar.SECOND, 0);
        }
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    
    /**
     * 获取当天00:00.
     */
    public static Date getCurrentDay() {
        return getLastSomeDayDate(0, false);
    }
    
}
