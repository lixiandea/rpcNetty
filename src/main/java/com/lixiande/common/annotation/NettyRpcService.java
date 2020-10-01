package com.lixiande.common.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
* @program: NettyRpcService
*
* @description: annotation for service interface :
 * include: service name and version
*
* @author: LiXiande
*
* @create: 14:53 2020/9/30
**/


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface NettyRpcService {
    Class<?> value();
    String version() default "";
}
