package com.devil.fission.machine.message;

/**
 * 消息类型枚举.
 *
 * @author Devil
 * @date Created in 2024/6/25 上午10:08
 */
public enum MessageTypeEnum {
    
    /**
     * 短信.
     */
    SMS(1, "短信"),
    
    /**
     * 邮件.
     */
    EMAIL(2, "邮件"),
    
    /**
     * IM消息.
     */
    IM(3, "站内信");
    
    /**
     * code码.
     */
    private final int code;
    
    /**
     * 描述.
     */
    private final String desc;
    
    MessageTypeEnum(int code, String desc) {
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
