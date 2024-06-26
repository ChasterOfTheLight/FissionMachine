package com.devil.fission.machine.message.sms.emay.request;

/**
 * 亿美基础短信服务请求体.
 *
 * @author devil
 * @date Created in 2024/6/25 上午11:22
 */
public class SmsBaseRequest extends BaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 定时时间. yyyy-MM-dd HH:mm:ss.
     */
    private String timerTime;
    
    /**
     * 扩展码(不允许中文).
     */
    private String extendedCode;
    
    public String getTimerTime() {
        return timerTime;
    }
    
    public void setTimerTime(String timerTime) {
        this.timerTime = timerTime;
    }
    
    public String getExtendedCode() {
        return extendedCode;
    }
    
    public void setExtendedCode(String extendedCode) {
        this.extendedCode = extendedCode;
    }
    
}
