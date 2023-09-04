package com.devil.fission.machine.sentinel;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * sentinel开关.
 *
 * @author Devil
 * @date Created in 2022/4/12 16:43
 */
@Retention(RetentionPolicy.RUNTIME)
@Import(FissionSentinelRegistrar.class)
@Target(ElementType.TYPE)
public @interface EnableFissionSentinel {

}
