package com.cuit.enums;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 10:22
 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.checkerframework.checker.units.qual.A;

@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {
    SUCCESS(200,"远程调用成功"),
    FAIL(500,"远程调用失败");

    private final int code;
    private final String message;
}
