package com.devil.fission.machine.example.service.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 回调实体.
 *
 * @author Devil
 * @date Created in 2024/8/20 上午10:32
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JsonPojo implements Serializable {
    
    /**
     * 项目编号.
     */
    private String data;
    
}
