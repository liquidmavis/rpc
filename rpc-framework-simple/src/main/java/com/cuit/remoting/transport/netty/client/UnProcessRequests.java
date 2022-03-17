package com.cuit.remoting.transport.netty.client;

import com.cuit.remoting.dto.RpcRequest;
import com.cuit.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 16:58
 */

public class UnProcessRequests {
    private static final Map<String,CompletableFuture<RpcResponse<Object>>> UNPROCESS_REQUESTS = new ConcurrentHashMap<>();

    public void put(String requestId,CompletableFuture<RpcResponse<Object>> completableFuture){
        UNPROCESS_REQUESTS.put(requestId,completableFuture);
    }

    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> completableFuture = UNPROCESS_REQUESTS.remove(rpcResponse.getRequestId());
        if(completableFuture != null){
            completableFuture.complete(rpcResponse);
        }else{
            throw new IllegalStateException();
        }
    }
}
