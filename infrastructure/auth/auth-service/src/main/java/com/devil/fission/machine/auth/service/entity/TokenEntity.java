package com.devil.fission.machine.auth.service.entity;

import com.devil.fission.machine.common.enums.PlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * token实体.
 *
 * @author devil
 * @date Created in 2022/12/28 14:37
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenEntity implements Serializable {
    
    private static final long serialVersionUID = 5993869547588515233L;
    
    /**
     * token key.
     */
    private String tokenKey;
    
    /**
     * token.
     */
    private String token;
    
    /**
     * 用户id.
     */
    private String userId;
    
    /**
     * 用户名.
     */
    private String userName;
    
    /**
     * 用户登录平台.
     */
    private PlatformEnum loginPlatform;
    
    /**
     * 登录时间.
     */
    private Long loginTime;
    
    /**
     * 过期时间.
     */
    private Long expireTime;
    
}
