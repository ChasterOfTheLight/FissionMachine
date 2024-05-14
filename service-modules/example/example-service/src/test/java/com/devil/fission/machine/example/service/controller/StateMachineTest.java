package com.devil.fission.machine.example.service.controller;

import com.alibaba.cola.statemachine.Action;
import com.alibaba.cola.statemachine.Condition;
import com.alibaba.cola.statemachine.StateMachine;
import com.alibaba.cola.statemachine.builder.StateMachineBuilder;
import com.alibaba.cola.statemachine.builder.StateMachineBuilderFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 状态机测试.
 *
 * @author Devil
 * @date Created in 2024/4/21 8:11
 */
public class StateMachineTest {
    
    // 订单状态（States)
    enum OrderState {
        INIT,
        PAID,
        DELIVERED,
        REFUNDED;
    }
    
    // 订单事件（Events）
    enum OrderEvent {
        PAY_SUCCESS,
        PAY_FAIL,
        DELIVERED_SUCCESS,
        REFUND_SUCCESS;
    }
    
    // 订单（Context)
    static class Order {
        
        String operator = "frank";
        
        String orderId = "123465";
        
        @Override
        public String toString() {
            return "Order{" + "operator='" + operator + '\'' + ", orderId='" + orderId + '\'' + '}';
        }
    }
    
    static final String MACHINE_ID = "orderStateMachine";
    
    @Test
    public void testExternalNormal() {
        // 第一步：生成一个状态机builder
        StateMachineBuilder<OrderState, OrderEvent, Order> builder = StateMachineBuilderFactory.create();
        
        // 第二步：设置一个外部状态转移类型的builder，并设置from\to\on\when\perform
        builder.externalTransition().from(OrderState.INIT).to(OrderState.PAID).on(OrderEvent.PAY_SUCCESS).when(checkCondition()).perform(doAction());
        
        // 第三步：设置状态机的id，并在StateMachineFactory中的stateMachineMap进行注册
        StateMachine<OrderState, OrderEvent, Order> stateMachine = builder.build(MACHINE_ID);
        
        // 第四步：触发状态机
        OrderState target = stateMachine.fireEvent(OrderState.INIT, OrderEvent.PAY_SUCCESS, new Order());
        assertEquals(OrderState.PAID, target);
    }
    
    private Condition<Order> checkCondition() {
        return order -> {
            System.out.println("Check condition : " + order);
            return true;
        };
    }
    
    private Action<OrderState, OrderEvent, Order> doAction() {
        return (from, to, event, ctx) -> {
            System.out.println(ctx.operator + " is operating " + ctx.orderId + " from:" + from + " to:" + to + " on:" + event);
        };
    }
    
}
