package com.cuit.remoting.dto;

import lombok.*;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 10:26
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class RpcMessage {
    private byte messageType;
    //序列化类型
    private byte codec;

    private byte compress;

    private int requestId;

    private Object data;
}
