package com.devil.fission.machine.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * token dto.
 *
 * @author devil
 * @date Created in 2022/12/28 14:52
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto implements Serializable {
    
    private static final long serialVersionUID = -4020821664457138117L;
    
    /**
     * token.
     */
    private String accessToken;
    
    /**
     * 过期时间戳.
     */
    private Long expiresIn;
    
}
