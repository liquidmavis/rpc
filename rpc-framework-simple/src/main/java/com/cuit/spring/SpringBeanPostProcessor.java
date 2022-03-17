package com.cuit.spring;

import com.cuit.annotation.RpcReference;
import com.cuit.annotation.RpcService;
import com.cuit.config.RpcServiceConfig;
import com.cuit.extension.ExtensionLoader;
import com.cuit.factory.SingleFactory;
import com.cuit.provider.ServiceProvider;
import com.cuit.provider.impl.ZkServiceProviderImpl;
import com.cuit.proxy.RpcClientProxy;
import com.cuit.remoting.transport.RpcRequestTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 查看类是否有我们自定义的注解，有的话做特殊操作
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/8 14:12
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingleFactory.getSingle(ZkServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean.getClass().isAnnotationPresent(RpcService.class)){
            log.info("该[{}]有自定义的{}注解",bean.getClass().getName(),RpcService.class.getCanonicalName());
            RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder().group(annotation.group())
                    .service(bean)
                    .version(annotation.version()).build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field field : declaredFields) {
            RpcReference annotation = field.getAnnotation(RpcReference.class);
            if(annotation != null){
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder().version(annotation.version())
                        .group(annotation.group()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient,rpcServiceConfig);
                Object proxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
