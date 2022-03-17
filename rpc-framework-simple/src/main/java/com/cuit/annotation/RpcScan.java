package com.cuit.annotation;

import com.cuit.spring.CustomerScannerRegistra;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/8 10:40
 */

@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomerScannerRegistra.class)
public @interface RpcScan {
    String[] basePackages();
}
