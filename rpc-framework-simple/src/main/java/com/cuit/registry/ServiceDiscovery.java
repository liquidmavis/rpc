package com.cuit.registry;

import com.cuit.extension.SPI;
import com.cuit.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 17:15
 */
@SPI
public interface ServiceDiscovery {
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
