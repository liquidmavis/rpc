package com.cuit.remoting.transport;

import com.cuit.extension.SPI;
import com.cuit.remoting.dto.RpcRequest;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 16:23
 */
@SPI
public interface RpcRequestTransport {
    Object sendRPCRequest(RpcRequest rpcRequest);
}
