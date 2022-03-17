package com.cuit.compress;

import com.cuit.extension.SPI;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/7 16:47
 */
@SPI
public interface Compress {
    byte[] compress(byte[] data);

    byte[] decompress(byte[] data);
}
