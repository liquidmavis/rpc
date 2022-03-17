package com.cuit.provider.impl;

import com.cuit.config.RpcServiceConfig;
import com.cuit.enums.RpcErrorMessageEnum;
import com.cuit.exception.RpcException;
import com.cuit.extension.ExtensionLoader;
import com.cuit.provider.ServiceProvider;
import com.cuit.registry.ServiceRegistry;
import com.cuit.remoting.transport.netty.client.NettyRpcClient;
import com.cuit.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/7 14:36
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider{

    /**
     * key: rpcservicename(接口名字+版本+组号)
     * value: 服务对象
     */

    private final Map<String,Object> serviceMap;
    private final Set<String> registeredServices;
    private final ServiceRegistry serviceRegistry;;

    public ZkServiceProviderImpl() {
        serviceMap = new ConcurrentHashMap<String,Object>();
        registeredServices = ConcurrentHashMap.newKeySet();
        serviceRegistry =  ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registeredServices.contains(rpcServiceName)){
            return;
        }
        registeredServices.add(rpcServiceName);
        serviceMap.put(rpcServiceName,rpcServiceConfig.getService());
        log.info("添加服务:{}和其接口",rpcServiceName,rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object o = serviceMap.get(rpcServiceName);
        if(o == null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return o;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registryService(rpcServiceConfig.getRpcServiceName(),new InetSocketAddress(hostAddress, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
