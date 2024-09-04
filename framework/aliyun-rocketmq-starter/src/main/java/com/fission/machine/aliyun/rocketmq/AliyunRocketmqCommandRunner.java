package com.fission.machine.aliyun.rocketmq;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.fission.machine.aliyun.rocketmq.config.AliyunRocketMqProperties;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * 初始化consumer执行器.
 *
 * @author Devil
 * @date Created in 2024/9/4 10:30
 */
@Slf4j
public class AliyunRocketmqCommandRunner implements ApplicationContextAware, CommandLineRunner {
    
    private final AliyunRocketMqProperties aliyunRocketMqProperties;
    
    public AliyunRocketmqCommandRunner(AliyunRocketMqProperties aliyunRocketMqProperties) {
        this.aliyunRocketMqProperties = aliyunRocketMqProperties;
    }
    
    @Override
    public void run(String... args) throws Exception {
        try {
            ConsumerBean consumerBean = applicationContext.getBean(ConsumerBean.class);
            consumerBean.setSubscriptionTable(assembleSubscriptionTable());
            consumerBean.start();
            log.info("aliyun rocketmq consumer start success");
        } catch (Exception e) {
            log.error("aliyun rocketmq consumer start error", e);
        }
    }
    
    private Map<Subscription, MessageListener> assembleSubscriptionTable() {
        Map<Subscription, MessageListener> subscriptionTable = new HashMap<>(16);
        Map<String, AbstractAliyunRocketmqMessageListener> beansOfType = applicationContext.getBeansOfType(AbstractAliyunRocketmqMessageListener.class);
        beansOfType.forEach((key, value) -> {
            Subscription subscription = new Subscription();
            subscription.setTopic(aliyunRocketMqProperties.getTopic());
            subscription.setExpression(aliyunRocketMqProperties.getTag());
            subscriptionTable.put(subscription, value);
            log.info("aliyun rocketmq consumer subscription: {} listener: {}", subscription, value);
        });
        return subscriptionTable;
    }
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
