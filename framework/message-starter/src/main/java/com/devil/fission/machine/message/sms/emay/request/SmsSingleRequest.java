package com.devil.fission.machine.message.sms.emay.request;

/**
 * 单条短信发送参数.
 *
 * @author devil
 * @date Created in 2024/6/25 上午11:22
 */
public class SmsSingleRequest extends SmsBaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 电话号码.
     */
    private String mobile;
    
    /**
     * 短信内容.
     */
    private String content;
    
    /**
     * 自定义smsid.
     */
    private String customSmsId;
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getCustomSmsId() {
        return customSmsId;
    }
    
    public void setCustomSmsId(String customSmsId) {
        this.customSmsId = customSmsId;
    }
    
}
