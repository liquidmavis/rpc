package com.cuit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/6 16:31
 */
@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {

    KYRO((byte)0x01,"kyro"),
    PROTOSTUFF((byte)0x02,"protostuff");

    private final byte code;
    private final String name;

    public static String getName(byte code){
        for(SerializationTypeEnum type : SerializationTypeEnum.values()){
            if(type.getCode() == code){
                return type.name;
            }
        }
        return null;
    }
}
