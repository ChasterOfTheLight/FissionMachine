package com.devil.fission.machine.common.enums;

import java.util.Objects;

/**
 * 登录平台枚举.
 *
 * @author devil
 * @date Created in 2022/12/27 16:38
 */
public enum PlatformEnum {
    
    /**
     * 小程序端.
     */
    MINI_PROGRAM("小程序", true, true),
    /**
     * App端.
     */
    APP("应用App", true, true),
    /**
     * openapi端（内部通过accessKey方式调用）.
     */
    OPENAPI("openApi", false, true),
    /**
     * 运营平台端.
     */
    SYSTEM_ADMIN("运营后台", false, false),
    /**
     * 无效.
     */
    NONE("无效", false, false);
    
    /**
     * 枚举说明.
     */
    private final String description;
    
    /**
     * 可否被踢.
     */
    private final boolean isKick;
    
    /**
     * 是否刷新token.
     */
    private final boolean isRefreshToken;
    
    /**
     * the description.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * get the isKick.
     */
    public boolean isKick() {
        return isKick;
    }
    
    /**
     * get the isRefreshToken.
     */
    public boolean isRefreshToken() {
        return isRefreshToken;
    }
    
    PlatformEnum(String description, boolean isKick, boolean isRefreshToken) {
        this.description = description;
        this.isKick = isKick;
        this.isRefreshToken = isRefreshToken;
    }
    
    /**
     * 根据平台值获取平台枚举类型.
     */
    public static PlatformEnum getByValue(String value) {
        for (PlatformEnum scopeEnum : PlatformEnum.values()) {
            if (Objects.equals(scopeEnum.toString(), value)) {
                return scopeEnum;
            }
        }
        return NONE;
    }
    
}
