package com.cuit.remoting.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 10:13
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1408507491492926L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] parameterTypes;
    private String version;
    private String group;

    public String getRpcServiceName() {
        return this.getInterfaceName()+this.getGroup()+this.getVersion();
    }
}
