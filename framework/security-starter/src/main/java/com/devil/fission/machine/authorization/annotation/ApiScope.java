package com.devil.fission.machine.authorization.annotation;

import com.devil.fission.machine.common.enums.PlatformEnum;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口作用范围.
 *
 * @author Devil
 * @date Created in 2023/3/21 9:30
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiScope {
    
    /**
     * 平台数组.
     */
    @AliasFor(value = "platforms")
    PlatformEnum[] values() default {};
    
    /**
     * 平台数组.
     */
    @AliasFor(value = "values")
    PlatformEnum[] platforms() default {};

}
