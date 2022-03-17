package com.cuit.proxy;

import com.cuit.config.RpcServiceConfig;
import com.cuit.enums.RpcErrorMessageEnum;
import com.cuit.enums.RpcResponseCodeEnum;
import com.cuit.exception.RpcException;
import com.cuit.remoting.dto.RpcRequest;
import com.cuit.remoting.dto.RpcResponse;
import com.cuit.remoting.transport.RpcRequestTransport;
import com.cuit.remoting.transport.netty.client.NettyRpcClient;
import io.netty.util.internal.ObjectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/8 14:24
 */

@Slf4j
public class RpcClientProxy implements InvocationHandler{

    private static final String INTERFACE_NAME = "interfaceName";
    private final RpcRequestTransport rpcClient;
    private final RpcServiceConfig rpcServiceConfig;

    public RpcClientProxy(RpcRequestTransport rpcClient, RpcServiceConfig rpcServiceConfig) {
        this.rpcClient = rpcClient;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    public <T>T getProxy(Class<T> clazz){
        return (T)Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this);
    }

    @SneakyThrows
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .methodName(method.getName())
                .interfaceName(method.getDeclaringClass().getName())
                .parameters(args)
                .parameterTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString()).build();
        RpcResponse rpcResponse = null;
        if(rpcClient instanceof NettyRpcClient){
            CompletableFuture<RpcResponse<Object>> rpcResponseCompletableFuture = (CompletableFuture<RpcResponse<Object>>) rpcClient.sendRPCRequest(rpcRequest);
            rpcResponse = rpcResponseCompletableFuture.get();
        }
        this.check(rpcResponse,rpcRequest);
        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest){
        if(rpcResponse == null || !Objects.equals(rpcResponse.getCode(), RpcResponseCodeEnum.SUCCESS.getCode())){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILED,INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }
        if(!Objects.equals(rpcRequest.getRequestId(), rpcResponse.getRequestId())){
            throw new RpcException((RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE),INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }

    }
}
