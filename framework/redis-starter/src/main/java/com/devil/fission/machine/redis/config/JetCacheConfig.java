package com.devil.fission.machine.redis.config;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.autoconfigure.JetCacheAutoConfiguration;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.redis.lettuce.RedisLettuceCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableMethodCache(basePackages = "com.devil.fission", order = -1)
@EnableCreateCacheAnnotation
@AutoConfigureBefore(value = {JetCacheAutoConfiguration.class})
public class JetCacheConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JetCacheConfig.class);
    
    @Value("${spring.redis.host}")
    private String host;
    
    @Value("${spring.redis.port}")
    private int port;
    
    @Value("${spring.redis.database}")
    private int database;
    
    @Value("${spring.redis.timeout}")
    private int timeout;
    
    @Value("${spring.redis.password}")
    private String password;
    
    @Bean
    @ConditionalOnMissingBean
    public RedisClient redisClient() {
        RedisURI redisuri = RedisURI.create(host, port);
        redisuri.setPassword(password);
        redisuri.setDatabase(database);
        redisuri.setTimeout(Duration.ofMillis(timeout));
        RedisClient client = RedisClient.create(redisuri);
        client.setOptions(ClientOptions.builder().disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS).build());
        return client;
    }
    
    @Bean
    public SpringConfigProvider springConfigProvider() {
        return new SpringConfigProvider();
    }
    
    /**
     * jetcache个性配置，没有采用springboot方式，如果有个性配置自己构建GlobalCacheConfig.
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalCacheConfig config(RedisClient redisClient) {
        Map<String, CacheBuilder> localBuilders = new HashMap<>(4);
        // 本地缓存使用caffeine
        EmbeddedCacheBuilder localCacheBuilder = CaffeineCacheBuilder.createCaffeineCacheBuilder().keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .limit(1000).expireAfterWrite(10L, TimeUnit.MINUTES);
        localBuilders.put(CacheConsts.DEFAULT_AREA, localCacheBuilder);
        
        // 远程缓存使用redis
        Map<String, CacheBuilder> remoteBuilders = new HashMap<>(4);
        RedisLettuceCacheBuilder remoteCacheBuilder = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE).valueEncoder(JavaValueEncoder.INSTANCE).valueDecoder(JavaValueDecoder.INSTANCE)
                .keyPrefix("FissionMachine:").redisClient(redisClient);
        remoteBuilders.put(CacheConsts.DEFAULT_AREA, remoteCacheBuilder);
    
        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setLocalCacheBuilders(localBuilders);
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
        // 默认不统计
        globalCacheConfig.setStatIntervalMinutes(0);
        globalCacheConfig.setAreaInCacheName(false);
    
        LOGGER.info("Jetcache Config Init Success");
        return globalCacheConfig;
    }
}