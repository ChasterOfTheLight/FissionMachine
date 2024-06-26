package com.devil.fission.machine.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息dto.
 *
 * @author Devil
 * @date Created in 2024/6/25 上午10:33
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageDto {
    
    /**
     * 发送目标.
     */
    private String sendTarget;
    
    /**
     * 消息Id.
     */
    private String msgId;
    
    /**
     * 消息内容.
     */
    private String msgContent;
    
    /**
     * 透传信息.
     */
    private String extendCode;
    
    /**
     * 消息类型.
     */
    private MessageTypeEnum msgType;
    
    /**
     * 消息发送渠道.
     */
    private MessageChannelEnum msgChannel;
    
}
