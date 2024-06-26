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
     * @param sendMessageDto sendMessageDto
     */
    void sendMessage(SendMessageDto sendMessageDto);
    
}