package com.devil.fission.machine.object.storage.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LogUtils.
 *
 * @author Devil
 * @date Created in 2023/3/23 10:55
 */
public class LogUtils {
    
    /**
     * Default log.
     */
    public static final Logger DEFAULT_LOG = LoggerFactory.getLogger("com.devil.fission.machine.object.storage");
    
    /**
     * Alibaba log.
     */
    public static final Logger ALIBABA_LOG = LoggerFactory.getLogger("com.devil.fission.machine.object.storage.alibaba");
    
    /**
     * Tencent log.
     */
    public static final Logger TENCENT_LOG = LoggerFactory.getLogger("com.devil.fission.machine.object.storage.tencent");

}
