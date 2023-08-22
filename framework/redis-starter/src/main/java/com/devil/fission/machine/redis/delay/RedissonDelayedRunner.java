package com.devil.fission.machine.redis.delay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

/**
 * redisson延迟执行轮训.
 *
 * @author devil
 * @date Created in 2022/10/31 16:56
 */
public class RedissonDelayedRunner implements CommandLineRunner {
    
    @Autowired
    private RedissonDelayedHandlerExecutor handlerExecutor;
    
    @Override
    public void run(String... args) throws Exception {
        // 启动异步线程，防止springboot的callRunner流程卡住
        handlerExecutor.execute();
    }
}
