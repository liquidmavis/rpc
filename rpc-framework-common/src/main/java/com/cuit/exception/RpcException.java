package com.cuit.exception;

import com.cuit.enums.RpcErrorMessageEnum;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/7 10:57
 */

public class RpcException extends RuntimeException{
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum,String detail){
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }
    public RpcException(String message,Throwable throwable){super(message,throwable);}
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum){
        super(rpcErrorMessageEnum.getMessage());
    }
}
