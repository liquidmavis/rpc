package com.cuit.remoting.dto;

import com.cuit.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 10:19
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class RpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 8053500016700947431L;
    private String requestId;
    private Integer code;
    private String message;

    private T data;

    public static <T> RpcResponse<T> success(T data,String requestId){
        RpcResponse<T> tRpcResponse = new RpcResponse<>();
        tRpcResponse.setRequestId(requestId);
        tRpcResponse.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        tRpcResponse.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        if(data != null){
            tRpcResponse.setData(data);
        }
        return tRpcResponse;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum code){
        RpcResponse<T> tRpcResponse = new RpcResponse<>();
        tRpcResponse.setMessage(code.getMessage());
        tRpcResponse.setCode(code.getCode());
        return tRpcResponse;
    }

}
