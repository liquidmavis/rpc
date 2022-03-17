package com.cuit.serialize;

import com.cuit.extension.SPI;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/7 16:43
 */
@SPI
public interface Serializer {
    byte[] serialize(Object object);

    <T>T deserialize(byte[] bytes,Class<T> clazz);
}
