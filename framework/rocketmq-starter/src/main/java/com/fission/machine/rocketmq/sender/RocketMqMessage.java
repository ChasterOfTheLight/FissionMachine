package com.fission.machine.rocketmq.sender;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RocketMqMessage.
 *
 * @author Devil
 * @date Created in 2024/8/15 下午5:08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RocketMqMessage implements Serializable {
    
    /**
     * 消息id 当做rocketmq的消息key.
     */
    private String messageId;
    
    /**
     * 业务id.
     */
    private String bizId;
    
    /**
     * 业务名称.
     */
    private String bizName;
    
    /**
     * 主题名称.
     */
    private String topicName;
    
    /**
     * 标签数组 为消息设置的标志，用于同一主题下区分不同类型的消息.
     */
    private String[] tags;
    
    /**
     * 消息体内容.
     */
    private String data;
    
    /**
     * 延迟消费消费毫秒数.
     */
    private int delayMilliSecond;

}
