package com.devil.fission.machine.example.service.event;

import com.lmax.disruptor.EventHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * ExampleEventHandler.
 *
 * @author devil
 * @date Created in 2024/5/14 16:05
 */
@Slf4j
public class ExampleEventHandler implements EventHandler<ExampleEvent> {
    
    @Override
    public void onEvent(ExampleEvent event, long sequence, boolean endOfBatch) throws Exception {
        // 务必使用try catch包装  不然有可能报错后导致disruptor环占满无法处理请求
        try {
            String value = event.getValue();
            log.info("value : {}", value);
        } catch (Exception e) {
            log.error("error", e);
        }
    }
}
