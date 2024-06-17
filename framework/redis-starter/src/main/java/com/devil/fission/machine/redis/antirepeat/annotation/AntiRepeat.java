package com.devil.fission.machine.redis.antirepeat.annotation;

import com.devil.fission.machine.redis.antirepeat.exception.RepeatException;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 基于获取锁的方式来控制防重复的请求， 获取锁成功代表本次请求不是重复的请求， 获取锁失败则代表本次请求是重复的一次请求， 应该被阻止或作出应对处理。 可以通过AntiRepeatContext来获取当前请求是否获取成功了锁
 * 支持嵌套锁 如果嵌套的方法是同一个类下需要额外处理（Spring AOP 本身限制，处理方式参考Spring通用的处理方法） 支持重入锁 相同完整key名的锁在同一个线程中多次获取，第一次获取成功后，剩余次数不需要重新获取锁的，视为可重入  注意：不支持分布式的重入，如对其他服务的rest、fegin调用
 * 请避免使用注解的方法自身递归调用，如果必须如此，请参考嵌套锁的AOP限制问题处理
 * 本注解也可用用于简单的限流，如设置expireTime，实现每多少expireTime可访问一次。设置100ms一次，并关闭完成后自动释放锁，即每秒10次的限流.
 *
 * @author devil
 * @date 2024/06/17 10:09
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AntiRepeat {
    
    /**
     * key的前缀，一般是业务描述的英文缩写或单词 如：userRegister 未设置时默认取拦截的类名:方法名.
     *
     * @return key前缀
     */
    String keyPrefix() default "";
    
    /**
     * 支持spel表达式，参数从方法入参获取  如：#id  #user.mobile key的组成一般由能表示当前提交的唯一性的参数组成，如：手机号、用户Id、业务Id等 最终用于锁的key会由keyPrefix:key组成
     * 未设置时会默认获取所有参数并hash后作为唯一值.
     *
     * @return key
     */
    String key() default "";
    
    /**
     * 执行条件，支持spel表达式，参数从方法入参获取.
     *
     * @return 执行条件
     */
    String condition() default "";
    
    /**
     * 等待获取锁的时间，默认为0s.
     *
     * @return 锁等待时间
     */
    long waitTime() default 0;
    
    /**
     * 锁持有的过期时间，超时释放 默认1秒，意味着1秒内相同的key的请求只允许一次.
     *
     * @return 锁过期时间
     */
    long expireTime() default 1;
    
    /**
     * 时间单位.
     *
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    
    /**
     * 相同key的锁是否支持可重入，默认是 可重入的锁不需要重新获取.
     *
     * @return 是否可重入
     */
    boolean isReentrant() default true;
    
    /**
     * 请求完成后自动释放锁，默认是 否则超时后自动释放锁.
     *
     * @return 是否在请求完成后自动释放锁
     */
    boolean isUnLockOnFinish() default true;
    
    /**
     * 获取锁失败时是否抛出异常，默认为否.
     *
     * @return 是否抛出异常
     */
    boolean isThrowExceptionOnFail() default true;
    
    /**
     * 获取锁失败的异常类，默认为RepeatException.
     *
     * @return 异常类
     */
    Class<? extends RuntimeException> failException() default RepeatException.class;
    
}
