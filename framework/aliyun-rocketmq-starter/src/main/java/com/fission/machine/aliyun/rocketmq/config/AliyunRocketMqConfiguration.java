package com.fission.machine.aliyun.rocketmq.config;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.fission.machine.aliyun.rocketmq.AliyunRocketMqMessageSender;
import com.fission.machine.aliyun.rocketmq.AliyunRocketmqCommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 阿里云 rocketmq access配置.
 *
 * @author Devil
 * @date Created in 2024/8/19 下午2:57
 */
@Configuration
@ConditionalOnProperty(prefix = "aliyun-rocketmq", name = "nameSrvAddr")
@EnableConfigurationProperties(AliyunRocketMqProperties.class)
public class AliyunRocketMqConfiguration {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AliyunRocketMqConfiguration.class);
    
    @Autowired
    private AliyunRocketMqProperties aliyunRocketMqProperties;
    
    /**
     * 消息发送.
     */
    @Bean(destroyMethod = "shutdown")
    public ProducerBean buildProducer() {
        ProducerBean producer = new ProducerBean();
        producer.setProperties(aliyunRocketMqProperties.getAliyunProperties());
        producer.start();
        LOGGER.info("aliyun rocketmq producer start success");
        return producer;
    }
    
    @Bean
    public AliyunRocketMqMessageSender aliyunRocketMqMessageSender(ProducerBean producerBean) {
        return new AliyunRocketMqMessageSender(producerBean);
    }
 
    /**
     * 消息消费.
     */
    @Bean(destroyMethod = "shutdown")
    public ConsumerBean buildConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        // 配置文件
        Properties properties = aliyunRocketMqProperties.getAliyunProperties();
        properties.setProperty(PropertyKeyConst.GROUP_ID, aliyunRocketMqProperties.getGroupId());
        // 将消费者线程数固定为20个 20为默认值
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "20");
        consumerBean.setProperties(properties);
        // 订阅关系
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<>(16);
        Subscription subscription = new Subscription();
        subscription.setTopic(aliyunRocketMqProperties.getTopic());
        subscription.setExpression(aliyunRocketMqProperties.getTag());
        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }
    
    @Bean
    public AliyunRocketmqCommandRunner aliyunRocketmqCommandRunner(AliyunRocketMqProperties aliyunRocketMqProperties) {
        return new AliyunRocketmqCommandRunner(aliyunRocketMqProperties);
    }
    
}
