package com.devil.fission.machine.message;

/**
 * 消息渠道枚举.
 *
 * @author Devil
 * @date Created in 2024/6/25 上午10:15
 */
public enum MessageChannelEnum {
    
    /**
     * 短信通道-亿美.
     */
    SMS_EMAY(1001, "亿美短信"),
    
    /**
     * 短信通道-阿里云.
     */
    SMS_ALIYUN(1002, "阿里云短信"),
    
    /**
     * 短信通道-腾讯云.
     */
    SMS_TENCENT(1003, "腾讯云短信"),
    
    /**
     * 邮件通道-阿里云.
     */
    EMAIL_ALIYUN(2001, "阿里云邮件"),
    
    /**
     * IM通道-腾讯云.
     */
    IM_TENCENT(3001, "腾讯云IM");
    
    /**
     * code码.
     */
    private final int code;
    
    /**
     * 描述.
     */
    private final String desc;
    
    MessageChannelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * get the code .
     */
    public int getCode() {
        return code;
    }
    
    /**
     * get the desc .
     */
    public String getDesc() {
        return desc;
    }
}
