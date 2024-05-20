package com.devil.fission.machine.example.service.rule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link OrderComputeRules } unit test.
 *
 * @author Devil
 * @date Created in 2024/5/20 10:47
 */
public class OrderComputeRulesTest {
    
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * 商品信息
     */
    public static final Product P1 = new Product("P1001", "Milk 12*220ML", 40 * 100);
    
    public static final Product P2 = new Product("P1002", "Keyboard T220", 120 * 100);
    
    public static final Product P3 = new Product("P1003", "Pod 2022", 200 * 100);
    
    /**
     * 优惠券
     */
    public static final Coupon C1 = new Coupon("满200减10", 200 * 100, 10 * 100);
    
    public static final Coupon C2 = new Coupon("满300减20", 300 * 100, 20 * 100);
    
    /**
     * 积分
     */
    public static final Integer S1 = 100;
    
    private static final Integer S2 = 350;
    
    @Test
    public void testRules() {
        /**
         * 构造数据
         */
        Facts facts = new Facts();
        
        //构造订单： 2种商品，共3件，总价200
        Order order = new Order();
        order.setOrderNo("001");
        List<Order.ProductItem> productItems = new ArrayList<>();
        productItems.add(new Order.ProductItem(P1, 2));
        productItems.add(new Order.ProductItem(P2, 1));
        order.setProductItems(productItems);
        facts.put("order", order);
        
        //构造账单：
        facts.put("bill", new Bill());
        //构建优惠券
        List<Coupon> coupons = new ArrayList<>();
        coupons.add(C1);
        coupons.add(C2);
        facts.put("coupons", coupons);
        
        //构建积分
        facts.put("score", S1);
        
        /**
         * 构造规则
         */
        Rules rules = OrderComputeRules.buildOrderComputeRules();
        
        /**
         * 执行规则计算
         */
        RulesEngine rulesEngine = new DefaultRulesEngine();
        rulesEngine.fire(rules, facts);
        
        /**
         * 输出结果
         */
        System.out.println("facts :");
        System.out.println(GSON.toJson(facts));
    }
    
}