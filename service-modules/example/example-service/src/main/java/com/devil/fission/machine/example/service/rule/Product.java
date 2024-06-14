package com.devil.fission.machine.example.service.rule;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 商品信息.
 *
 * @author Devil
 * @date Created in 2024/5/20 10:41
 */
@Data
@AllArgsConstructor
public class Product {
    
    /**
     * 商品编号.
     */
    private String sku;
    
    /**
     * 商品名称.
     */
    private String name;
    
    /**
     * 销售价（单位：分）.
     */
    private Integer price;
    
}
