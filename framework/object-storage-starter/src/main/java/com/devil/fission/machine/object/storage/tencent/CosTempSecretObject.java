package com.devil.fission.machine.object.storage.tencent;

import lombok.Data;

/**
 * CosTempSecretObject.
 *
 * @author Devil
 * @date Created in 2023/3/22 11:13
 */
@Data
public class CosTempSecretObject {
    
    /**
     * 临时SecretId.
     */
    private String tmpSecretId;
    
    /**
     * 临时SecretKey.
     */
    private String tmpSecretKey;
    
    /**
     * 临时sessionToken.
     */
    private String sessionToken;
    
    /**
     * startTime.
     */
    private String startTime;
    
    /**
     * expiredTime.
     */
    private String expiredTime;
    
}
