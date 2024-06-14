package com.devil.fission.machine.example.service.rule;

import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;

import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;

/**
 * 规则引擎规则.
 *
 * @author Devil
 * @date Created in 2024/5/20 10:42
 */
public class OrderComputeRules {
    
    /**
     * 构造订单计算规则.
     *
     * @return 规则集合
     */
    public static Rules buildOrderComputeRules() {
        Rules rules = new Rules();
        rules.register(new ProductAmountRule());
        rules.register(new FreightRule());
        rules.register(new PayableRule());
        rules.register(new CouponRule());
        rules.register(new ScoreCashRule());
        rules.register(new PaymentRule());
        return rules;
    }
    
    @Rule(name = "ProductAmountComputeRule", description = "商品金额计算规则", priority = 1)
    public static class ProductAmountRule {
        
        /**
         * 规则执行条件.
         */
        @Condition
        public boolean when() {
            return true;
        }
        
        /**
         * 规则执行方法.
         */
        @Action
        public void then(Facts facts) throws Exception {
            Bill bill = facts.get("bill");
            Order order = facts.get("order");
            Integer productAmount = order.getProductItems().stream().map(item -> item.getProduct().getPrice() * item.getNumber())
                    .reduce(0, (BinaryOperator<Integer>) Integer::sum, Integer::sum);
            bill.setProductAmount(productAmount);
        }
    }
    
    @Rule(name = "FreightComputeRule", description = "运费金额计算规则", priority = 2)
    public static class FreightRule {
        
        /**
         * 规则执行条件.
         */
        @Condition
        public boolean when(@Fact("bill") Bill bill) {
            return bill.getProductAmount() < 99 * 100;
        }
        
        /**
         * 规则执行方法.
         */
        @Action
        public void then(Facts facts) throws Exception {
            Bill bill = facts.get("bill");
            bill.setFreight(8 * 100);
        }
    }
    
    @Rule(name = "PayableComputeRule", description = "应付金额计算规则", priority = 3)
    public static class PayableRule {
        
        /**
         * 规则执行条件.
         */
        @Condition
        public boolean when() {
            return true;
        }
        
        /**
         * 规则执行方法.
         */
        @Action
        public void then(Facts facts) throws Exception {
            Bill bill = facts.get("bill");
            bill.setPayable(bill.getProductAmount() + bill.getFreight());
        }
    }
    
    @Rule(name = "CouponComputeRule", description = "计算优惠券优惠券", priority = 4)
    public static class CouponRule {
        
        /**
         * 规则执行条件.
         */
        @Condition
        public boolean when(@Fact("coupons") List<Coupon> coupons) {
            return Objects.nonNull(coupons) && !coupons.isEmpty();
        }
        
        /**
         * 规则执行方法.
         */
        @Action
        public void then(Facts facts) throws Exception {
            Bill bill = facts.get("bill");
            List<Coupon> coupons = facts.get("coupons");
            coupons.sort((o1, o2) -> -1 * o1.getThreshold().compareTo(o2.getThreshold()));
            for (Coupon coupon : coupons) {
                if (bill.getPayable() >= coupon.getThreshold()) {
                    bill.setDiscount(coupon.getDiscount());
                    break;
                }
            }
        }
    }
    
    @Rule(name = "ScoreCashComputeRule", description = "积分抵现计算规则", priority = 5)
    public static class ScoreCashRule {
        
        /**
         * 规则执行条件.
         */
        @Condition
        public boolean when(@Fact("score") Integer score) {
            return score >= 100;
        }
        
        /**
         * 规则执行方法.
         */
        @Action
        public void then(Facts facts) throws Exception {
            Bill bill = facts.get("bill");
            Integer score = facts.get("score");
            // 单位：元
            int cash = score / 100;
            bill.setScoreCash(cash * 100);
        }
    }
    
    @Rule(name = "PaymentComputeRule", description = "应支付金额计算规则", priority = 6)
    public static class PaymentRule {
        
        /**
         * 规则执行条件.
         */
        @Condition
        public boolean when() {
            return true;
        }
        
        /**
         * 规则执行方法.
         */
        @Action
        public void then(Facts facts) throws Exception {
            Bill bill = facts.get("bill");
            bill.setPayment(bill.getPayable() - bill.getDiscount() - bill.getScoreCash());
        }
    }
    
}
