package com.devil.fission.machine.auth.api.dto;

import com.devil.fission.machine.common.enums.PlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * token verify dto.
 *
 * @author devil
 * @date Created in 2022/12/28 14:52
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyTokenDto implements Serializable {
    
    private static final long serialVersionUID = 5226532401440951913L;
    
    /**
     * new token.
     */
    private String newToken;
    
    /**
     * 用户id.
     */
    private String userId;
    
    /**
     * 用户名.
     */
    private String userName;
    
    /**
     * 平台.
     */
    private PlatformEnum platform;
    
}
