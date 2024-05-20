package com.devil.fission.machine.example.service.rule;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 优惠券.
 *
 * @author Devil
 * @date Created in 2024/5/20 10:40
 */
@Data
@AllArgsConstructor
public class Coupon {
    
    /**
     * 优惠券描述
     */
    private String description;
    
    /**
     * 门槛
     */
    private Integer threshold;
    
    /**
     * 减免（单位：分）
     */
    private Integer discount;
    
}
