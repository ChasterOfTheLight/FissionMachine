package com.devil.fission.machine.auth.api.param;

import com.devil.fission.machine.common.enums.PlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 登录参数.
 *
 * @author devil
 * @date Created in 2022/12/28 16:51
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginParam implements Serializable {
    
    private static final long serialVersionUID = -6366829235748786645L;
    
    /**
     * 用户id.
     */
    @NotBlank(message = "用户id不能为空")
    private String userId;
    
    /**
     * 用户名.
     */
    @NotBlank(message = "用户名不能为空")
    private String userName;
    
    /**
     * 用户登录平台.
     */
    @NotNull(message = "用户登录平台不能为空")
    private PlatformEnum loginPlatform;
    
    /**
     * 登录ip地址.
     */
    @NotBlank(message = "登录ip地址不能为空")
    private String ipAddress;
    
    /**
     * 登录过期时间(秒) 比如60秒后过期，则为60.
     */
    @NotNull(message = "登录过期时间不能为空")
    private Long expireTimeSeconds;
    
}
