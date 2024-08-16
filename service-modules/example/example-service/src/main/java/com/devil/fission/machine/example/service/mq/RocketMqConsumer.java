package com.devil.fission.machine.example.service.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.stereotype.Service;

/**
 * RocketMqConsumer.
 *
 * @author Devil
 * @date Created in 2024/8/15 下午6:03
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "machine", consumerGroup = "FISSION_MACHINE")
public class RocketMqConsumer implements RocketMQListener<String>, RocketMQPushConsumerLifecycleListener {
    
    @Override
    public void onMessage(String message) {
        log.info("receive message: {}", message);
    }
    
    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        // 从最新处消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setConsumeTimestamp(UtilAll.timeMillisToHumanString3(System.currentTimeMillis()));
    }
}
