package com.cuit.annotation;

import java.lang.annotation.*;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/8 10:41
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {
    String version() default "";
    String group() default "";
 }
