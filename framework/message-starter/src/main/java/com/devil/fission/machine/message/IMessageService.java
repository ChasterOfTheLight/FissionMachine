package com.devil.fission.machine.message;

/**
 * 消息服务.
 *
 * @author Devil
 * @date Created in 2024/6/24 下午3:43
 */
public interface IMessageService {
    
    /**
     * 发送消息.
     *
     * @param customMsgId 自定义消息id
     * @param target      发送目标 （手机号、邮件等）
     * @param message     消息
     * @param extendCode  扩展码
     */
    void sendMessage(String customMsgId, String target, String message, String extendCode);
    
}