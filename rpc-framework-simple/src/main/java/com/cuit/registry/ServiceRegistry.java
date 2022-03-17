package com.cuit.registry;

import com.cuit.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 17:16
 */
@SPI
public interface ServiceRegistry {
    void registryService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}

