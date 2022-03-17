package com.cuit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/6 16:28
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {

    GZIP((byte)0x01,"gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code){
        for(CompressTypeEnum type : CompressTypeEnum.values()){
            if(type.getCode() == code){
                return type.name;
            }
        }
        return null;
    }
}
