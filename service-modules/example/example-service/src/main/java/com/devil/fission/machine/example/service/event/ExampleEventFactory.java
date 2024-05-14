package com.devil.fission.machine.example.service.event;

import com.lmax.disruptor.EventFactory;

/**
 * ExampleEventFactory.
 *
 * @author devil
 * @date Created in 2024/5/14 16:05
 */
public class ExampleEventFactory implements EventFactory<ExampleEvent> {
    
    @Override
    public ExampleEvent newInstance() {
        return new ExampleEvent();
    }
}
