package com.cuit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/7 10:57
 */
@AllArgsConstructor
@Getter
@ToString
public enum  RpcErrorMessageEnum {
    CLIENT_CONNECT_SERVER_FAILED("客户端连接失败"),
    SERVICE_INVOCATION_FAILED("服务调用失败"),
    SERVICE_CAN_NOT_BE_FOUND("没有发现服务"),
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务没有实现任何接口"),
    REQUEST_NOT_MATCH_RESPONSE("返回请求错误！请求和返回的不匹配");

    private final String message;
}
