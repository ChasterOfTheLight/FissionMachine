package com.devil.fission.machine.object.storage.alibaba;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * OssTempSecretObject.
 *
 * @author Devil
 * @date Created in 2023/3/22 11:09
 */
@ApiModel(value = "oss临时秘钥响应体")
@Data
public class OssTempSecretObject {
    
    /**
     * accessId.
     */
    @ApiModelProperty("accessId")
    private String accessId;
    
    /**
     * host.
     */
    @ApiModelProperty("host")
    private String host;
    
    /**
     * policy.
     */
    @ApiModelProperty("policy")
    private String policy;
    
    /**
     * signature.
     */
    @ApiModelProperty("signature")
    private String signature;
    
    /**
     * expire.
     */
    @ApiModelProperty("expire")
    private String expire;
    
    /**
     * 上传目录.
     */
    @ApiModelProperty("dir")
    private String dir;
    
    
}
