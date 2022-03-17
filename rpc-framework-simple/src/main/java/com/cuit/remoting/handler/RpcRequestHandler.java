package com.cuit.remoting.handler;

import com.cuit.enums.RpcErrorMessageEnum;
import com.cuit.exception.RpcException;
import com.cuit.factory.SingleFactory;
import com.cuit.provider.ServiceProvider;
import com.cuit.provider.impl.ZkServiceProviderImpl;
import com.cuit.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/7 11:15
 */
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        serviceProvider = SingleFactory.getSingle(ZkServiceProviderImpl.class);
    }

    /**
     * 返回请求的方法并且调用该远程方法
     * @param request
     * @return
     */
    public Object handler(RpcRequest request){
        Object service = serviceProvider.getService(request.getRpcServiceName());
        return invokeTargetMethod(request,service);
    }

    private Object invokeTargetMethod(RpcRequest request,Object service){
        Object result;
        try {
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
            result = method.invoke(service, request.getParameters());
            log.info("服务[{}]调用成功返回:[{}]",request.getInterfaceName(),request.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(),e);
        }
        return result;
    }
}
