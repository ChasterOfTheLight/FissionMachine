package com.devil.fission.machine.auth.api.enums;

import java.util.Objects;

/**
 * 访问来源枚举.
 *
 * @author devil
 * @date Created in 2022/12/27 16:38
 */
public enum AccessSourceEnum {
    
    /**
     * OPENAPI.
     */
    OPENAPI("openapi"),
    /**
     * INNER.
     */
    INNER("inner"),
    /**
     * 无效.
     */
    NONE("none");
    
    /**
     * 枚举值(确定了不再修改).
     */
    private final String value;
    
    AccessSourceEnum(String value) {
        this.value = value;
    }
    
    /**
     * 根据平台值获取平台枚举类型.
     */
    public static AccessSourceEnum getByValue(String value) {
        for (AccessSourceEnum scopeEnum : AccessSourceEnum.values()) {
            if (Objects.equals(scopeEnum.toString(), value)) {
                return scopeEnum;
            }
        }
        return NONE;
    }
    
    /**
     * get the value .
     */
    public String getValue() {
        return value;
    }
}
