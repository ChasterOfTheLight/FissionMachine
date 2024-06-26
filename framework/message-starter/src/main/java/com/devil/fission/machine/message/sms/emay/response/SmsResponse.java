package com.devil.fission.machine.message.sms.emay.response;

import java.io.Serializable;

/**
 * 单条短信发送响应.
 *
 * @author devil
 * @date Created in 2024/6/25 上午11:22
 */
public class SmsResponse implements Serializable {
    
    private static final long serialVersionUID = -7339112581692326482L;
    
    /**
     * 系统唯一smsId.
     */
    private String smsId;
    
    /**
     * 手机号.
     */
    private String mobile;
    
    /**
     * 自定义消息id.
     */
    private String customSmsId;
    
    public SmsResponse() {
    
    }
    
    public SmsResponse(String smsId, String mobile, String customSmsId) {
        this.smsId = smsId;
        this.mobile = mobile;
        this.customSmsId = customSmsId;
    }
    
    public String getSmsId() {
        return smsId;
    }
    
    public void setSmsId(String smsId) {
        this.smsId = smsId;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getCustomSmsId() {
        return customSmsId;
    }
    
    public void setCustomSmsId(String customSmsId) {
        this.customSmsId = customSmsId;
    }
    
    @Override
    public String toString() {
        return "SmsResponse{" + "smsId='" + smsId + '\'' + ", mobile='" + mobile + '\'' + ", customSmsId='" + customSmsId + '\'' + '}';
    }
}
