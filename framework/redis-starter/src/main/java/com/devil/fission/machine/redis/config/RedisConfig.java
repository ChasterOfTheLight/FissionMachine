package com.devil.fission.machine.redis.config;

import com.devil.fission.machine.redis.service.RedisService;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis配置.
 *
 * @author devil
 * @date Created in 2022/3/24 14:13
 */
@Configuration
@EnableCaching
@AutoConfigureBefore(value = {RedisAutoConfiguration.class})
public class RedisConfig extends CachingConfigurerSupport {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);
    
    public static Jackson2JsonRedisSerializer jackson2JsonRedisSerializer;
    public static StringRedisSerializer stringRedisSerializer;
    
    static {
        jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(new ObjectMapper().setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
                .activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY));
        stringRedisSerializer = new StringRedisSerializer();
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(stringRedisSerializer);
        // 使用json序列化redis的value值
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // 使用json序列化redis的value值
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public RedisService redisService(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        return new RedisService(redisTemplate, stringRedisTemplate);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        String url = "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort();
        config.useSingleServer().setAddress(url).setPassword(redisProperties.getPassword()).setDatabase(redisProperties.getDatabase())
                .setPingConnectionInterval(1000);
        
        try {
            RedissonClient redissonClient = Redisson.create(config);
            LOGGER.info("Redisson Client Init Success");
            return redissonClient;
        } catch (Exception e) {
            LOGGER.error("Redisson Client Init Failed url:[{}], Exception:", url, e);
            return null;
        }
    }
    
}
