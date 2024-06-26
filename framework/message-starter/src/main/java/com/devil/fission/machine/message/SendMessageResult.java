package com.devil.fission.machine.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送消息结果.
 *
 * @author Devil
 * @date Created in 2024/6/25 下午1:43
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendMessageResult {
    
    /**
     * 是否发送成功.
     */
    private boolean sendSuccess;
    
    /**
     * 发送失败原因.
     */
    private String failReason;
    
}
