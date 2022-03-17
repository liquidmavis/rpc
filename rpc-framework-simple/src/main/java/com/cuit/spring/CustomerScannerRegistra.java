package com.cuit.spring;

import com.cuit.annotation.RpcScan;
import com.cuit.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/8 10:36
 */
@Slf4j
public class CustomerScannerRegistra implements ImportBeanDefinitionRegistrar,ResourceLoaderAware {
    private ResourceLoader resourceLoader;
    private static final String SPRINT_BEAN_BASE_PACKAGE = "com.cuit";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackages";


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] rpcScanBasePackages = new String[0];
        if(annotationAttributes != null){
            rpcScanBasePackages = annotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        if(rpcScanBasePackages.length == 0){
            rpcScanBasePackages = new String[]{((StandardAnnotationMetadata)importingClassMetadata).getIntrospectedClass().getPackage().getName()};
        }
        //扫描RpcService注解
        CustomScanner rpcServiceCustomScanner = new CustomScanner(registry, RpcService.class);
        //扫描Componet注解
        CustomScanner componetCustomScanner = new CustomScanner(registry, Component.class);
        if(resourceLoader != null){
            rpcServiceCustomScanner.setResourceLoader(resourceLoader);
            componetCustomScanner.setResourceLoader(resourceLoader);
        }
        int scan = componetCustomScanner.scan(SPRINT_BEAN_BASE_PACKAGE);
        log.info("springbean扫描的数量:{}",scan);
        scan = rpcServiceCustomScanner.scan(rpcScanBasePackages);
        log.info("rpc远程调用bean扫描的数量:{}",scan);
    }
}
