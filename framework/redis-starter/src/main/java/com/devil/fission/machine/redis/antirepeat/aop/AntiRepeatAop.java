package com.devil.fission.machine.redis.antirepeat.aop;

import cn.hutool.crypto.SecureUtil;
import com.devil.fission.machine.redis.antirepeat.annotation.AntiRepeat;
import com.devil.fission.machine.redis.antirepeat.exception.NoLockImplException;
import com.devil.fission.machine.redis.antirepeat.lock.AntiRepeatLock;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 拦截AntiRepeat注解，实现AOP层面的防重复处理.
 *
 * @author devil
 * @date 2024/06/17 10:09
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class AntiRepeatAop {
    
    @Resource
    private AntiRepeatLock antiRepeatLock;
    
    @Pointcut("@annotation(com.tojoy.core.antirepeat.annotation.AntiRepeat)")
    public void aopAntiRepeat() {
    }
    
    /**
     * 防重复提交切面.
     */
    @Around(value = "aopAntiRepeat() && @annotation(antiRepeat)", argNames = "point,antiRepeat")
    public Object aroundAntiRepeat(ProceedingJoinPoint point, AntiRepeat antiRepeat) throws Throwable {
        
        //防重复提交需依赖实现AntiRepeatLock的分布式锁，默认支持redis实现
        if (antiRepeatLock == null) {
            throw new NoLockImplException();
        }
        
        //构建spel表达式上下文
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext(point.getArgs());
        setContextVariables(standardEvaluationContext, point);
        
        Boolean conditionResult = true;
        if (StringUtils.isNotBlank(antiRepeat.condition())) {
            conditionResult = getElValue(antiRepeat.condition(), standardEvaluationContext, Boolean.class);
            if (conditionResult == null) {
                conditionResult = true;
            }
        }
        
        if (!conditionResult) {
            return point.proceed();
        }
        
        String keyPrefix = antiRepeat.keyPrefix();
        //keyPrefix为空时，默认通过非全限定类名+方法名实现
        if (StringUtils.isBlank(keyPrefix)) {
            keyPrefix = point.getTarget().getClass().getSimpleName() + point.getSignature().getName();
        }
        String key = antiRepeat.key();
        //key为空时，默认通过全参数组合 md5 hash取值
        if (StringUtils.isBlank(key)) {
            if (point.getArgs().length > 0) {
                StringBuilder args = new StringBuilder("args:");
                Gson gson = new Gson();
                for (Object arg : point.getArgs()) {
                    args.append(gson.toJson(arg));
                }
                key = SecureUtil.md5(args.toString());
            }
        } else {
            //解析spel表达式的key
            key = getElValue(antiRepeat.key(), standardEvaluationContext, String.class);
            if (key == null) {
                key = "";
            }
        }
        
        //antiRepeatLock实现不同，可能key的结构有调整，所以需要重命名
        key = antiRepeatLock.renameKey(keyPrefix + ":" + key);
        
        //是否是当前层持有锁（防止非持有锁的嵌套层提前解锁，需要标记当前持有所有的一层）
        boolean isCurrentLock;
        //是否获取锁成功
        boolean lock = false;
        
        //如果支持重入锁
        if (antiRepeat.isReentrant()) {
            //看是否能获取当前key对应的锁
            lock = AntiRepeatContext.isGetLock(key);
        }
        if (lock) {
            isCurrentLock = false;
        } else {
            //尝试获取锁
            lock = antiRepeatLock.tryLock(key, antiRepeat.waitTime(), antiRepeat.expireTime(), antiRepeat.timeUnit());
            //标记为当前层持有锁（是否加锁成功不重要）
            isCurrentLock = true;
        }
        
        //如果是当前层
        if (isCurrentLock) {
            //存储加锁结果
            AntiRepeatContext.setResult(key, lock);
        }
        
        //如果没有获取到锁
        if (!lock) {
            //是否获取锁失败时抛出异常
            if (antiRepeat.isThrowExceptionOnFail()) {
                //获取锁失败，代表此次提交为重复的提交，抛出此异常
                throw antiRepeat.failException().newInstance();
            }
        }
        
        //记录上一调用链路层的key
        String prevKey = AntiRepeatContext.getCurrentKey();
        //存储接下来要访问的key
        AntiRepeatContext.setCurrentKey(key);
        //计数，统计是否是最外层的调用，用来清理上下文
        int count = AntiRepeatContext.incrCounter();
        try {
            return point.proceed();
        } finally {
            //恢复上一调用链路层的key
            AntiRepeatContext.setCurrentKey(prevKey);
            //如果获取锁成功，且是当前层获取的锁，则需要解锁
            if (isCurrentLock && lock) {
                try {
                    //解锁，可能会因为调用时间过长导致锁已经自动释放
                    antiRepeatLock.unLock(key);
                } catch (IllegalMonitorStateException ex) {
                    //已经超时
                }
            }
            //最外层的调用链路，结束时清理上下文，防止内存泄露的问题
            if (count == 0) {
                AntiRepeatContext.clean();
            }
        }
    }
    
    /**
     * 设置spel上下文参数.
     */
    private void setContextVariables(StandardEvaluationContext standardEvaluationContext, JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
        String[] parametersName = parameterNameDiscoverer.getParameterNames(targetMethod);
        
        if (args == null || args.length == 0) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            standardEvaluationContext.setVariable(parametersName[i], args[i]);
        }
    }
    
    /**
     * 获取spel内容.
     */
    private <T> T getElValue(String key, StandardEvaluationContext context, Class<T> clazz) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(key);
        return exp.getValue(context, clazz);
    }
}
