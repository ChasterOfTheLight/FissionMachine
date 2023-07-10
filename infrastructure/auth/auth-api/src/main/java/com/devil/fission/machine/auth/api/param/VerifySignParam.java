package com.devil.fission.machine.auth.api.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 校验sign参数.
 *
 * @author devil
 * @date Created in 2022/12/28 16:51
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifySignParam implements Serializable {
    
    private static final long serialVersionUID = 3378586833731092917L;
    
    @NotBlank(message = "accessKey不能为空")
    private String accessKey;
    
    @NotBlank(message = "timestamp不能为空")
    private String timestamp;
    
    @NotBlank(message = "随机数不能为空")
    private String nonce;
    
    @NotBlank(message = "sign不能为空")
    private String sign;
    
    @NotBlank(message = "访问uri不能为空")
    private String requestUri;
}
