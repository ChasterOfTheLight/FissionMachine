package com.devil.fission.machine.redis.delay;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * redisson延时队列工具开关.
 *
 * @author devil
 * @date Created in 2022/10/31 18:02
 */
@Retention(RetentionPolicy.RUNTIME)
@Import(RedissonDelayedConfiguration.class)
@Target(ElementType.TYPE)
public @interface EnableRedissonDelayed {

}
