package com.devil.fission.machine.message;

import cn.hutool.core.util.IdUtil;
import com.devil.fission.machine.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息服务抽象类.
 *
 * @author Devil
 * @date Created in 2024/6/24 下午3:45
 */
@Slf4j
public abstract class AbstractMessageService implements IMessageService {
    
    @Override
    public void sendMessage(String customMsgId, String target, String message, String extendCode) {
        if (StringUtils.isEmpty(target) || StringUtils.isEmpty(message)) {
            log.warn("发送消息目标或消息不能为空！");
            return;
        }
        MessageTypeEnum messageType = getMessageType();
        MessageChannelEnum messageChannel = getMessageChannel();
        if (messageType == null || messageChannel == null) {
            log.warn("发送消息类型或消息渠道不能为空！");
            return;
        }
        if (StringUtils.isEmpty(customMsgId)) {
            customMsgId = IdUtil.fastSimpleUUID();
        }
        SendMessageDto sendMessageDto = SendMessageDto.builder().msgId(customMsgId).sendTarget(target).msgType(messageType).msgChannel(messageChannel).msgContent(message)
                .extendCode(extendCode).build();
        sendChannelMessage(sendMessageDto);
    }
    
    /**
     * 发送渠道消息.
     *
     * @param sendMessageDto 发送渠道消息dto
     * @return 发送结果
     */
    protected abstract SendMessageResult sendChannelMessage(SendMessageDto sendMessageDto);
    
    /**
     * 获取消息类型.
     *
     * @return 消息类型
     */
    protected abstract MessageTypeEnum getMessageType();
    
    /**
     * 获取消息渠道.
     *
     * @return 消息渠道
     */
    protected abstract MessageChannelEnum getMessageChannel();
    
}