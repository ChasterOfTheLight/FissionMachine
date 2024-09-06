package com.fission.machine.aliyun.rocketmq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AliRocketMqMessage.
 *
 * @author Devil
 * @date Created in 2024/8/15 下午5:08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AliyunRocketMqMessage implements Serializable {
    
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
    private String tags;
    
    /**
     * 消息体内容.
     */
    private String data;

}
