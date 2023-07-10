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
 * 登出参数.
 *
 * @author devil
 * @date Created in 2022/12/28 16:51
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutParam implements Serializable {
    
    private static final long serialVersionUID = -7133361118625147644L;
    
    /**
     * 用户id.
     */
    @NotBlank(message = "用户id不能为空")
    private String userId;
    
    /**
     * 用户登录平台.
     */
    @NotNull(message = "用户登录平台不能为空")
    private PlatformEnum loginPlatform;
    
}
