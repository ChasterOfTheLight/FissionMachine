package com.devil.fission.machine.example.service.controller;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 回调基础实体.
 *
 * @author Devil
 * @date Created in 2024/8/21 下午6:06
 */
@Data
public class TCallbackPojo<T> implements Serializable {
    
    /**
     * 类型 INSERT | UPDATE.
     */
    private String type;
    
    /**
     * 数据数组.
     */
    private List<T> data;
    
}
