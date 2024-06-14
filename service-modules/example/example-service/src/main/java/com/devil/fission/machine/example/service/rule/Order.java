package com.devil.fission.machine.example.service.rule;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 订单.
 *
 * @author Devil
 * @date Created in 2024/5/20 10:42
 */
@Data
public class Order {
    
    /**
     * 订单号.
     */
    private String orderNo;
    
    /**
     * 商品清单.
     */
    private List<ProductItem> productItems;
    
    /**
     * 商品项.
     */
    @Data
    @AllArgsConstructor
    public static class ProductItem {
        
        /**
         * 商品.
         */
        private Product product;
        
        /**
         * 数量.
         */
        private Integer number;
    }
    
}
