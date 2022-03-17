package com.cuit.registry;

import com.cuit.enums.RpcErrorMessageEnum;
import com.cuit.exception.RpcException;
import com.cuit.extension.ExtensionLoader;
import com.cuit.loadbalance.Loadbalance;
import com.cuit.registry.util.CuratorUtils;
import com.cuit.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/6 16:40
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery{
    private Loadbalance loadbalance;

    public ZkServiceDiscoveryImpl() {
        this.loadbalance = (Loadbalance) ExtensionLoader.getExtensionLoader(Loadbalance.class).getExtension("loadBalance");
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if(childrenNodes == null || childrenNodes.size() == 0){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND,rpcServiceName);
        }
        String address = loadbalance.selectServiceAddress(childrenNodes, rpcRequest);
        log.info("成功找到服务的目标地址:{}",address);
        String[] split = address.split(":");
        String host  = split[0];
        int port = Integer.parseInt(split[1]);
        return new InetSocketAddress(host,port);
    }
}
