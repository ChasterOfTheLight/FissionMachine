package com.devil.fission.machine.example.service.rule;

import lombok.Data;

/**
 * 账单.
 *
 * @author Devil
 * @date Created in 2024/5/20 10:40
 */
@Data
public class Bill {
    
    /**
     * 商品金额
     */
    private Integer productAmount = 0;
    
    /**
     * 运费金额
     */
    private Integer freight = 0;
    
    /**
     * 应付金额 = 商品金额 + 运费金额
     */
    private Integer payable = 0;
    
    /**
     * 优惠金额
     */
    private Integer discount = 0;
    
    /**
     * 积分抵现
     */
    private Integer scoreCash = 0;
    
    /**
     * 实付金额 = 应付金额 - 优惠金额 - 积分抵现
     */
    private Integer payment = 0;
    
}
