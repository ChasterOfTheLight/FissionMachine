package com.devil.fission.machine.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 请求dto.
 *
 * @author Devil
 * @date Created in 2022/12/12 17:01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserDto implements Serializable {
    
    private static final long serialVersionUID = 310034476119495319L;
    
    private Long userId;
    
}
