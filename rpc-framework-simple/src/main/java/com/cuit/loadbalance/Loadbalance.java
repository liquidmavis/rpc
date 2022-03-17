package com.cuit.loadbalance;

import com.cuit.extension.SPI;
import com.cuit.remoting.dto.RpcRequest;

import java.util.List;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/6 16:44
 */
@SPI
public interface Loadbalance {
    public String selectServiceAddress(List<String> serviceAddressList,RpcRequest request);
}
