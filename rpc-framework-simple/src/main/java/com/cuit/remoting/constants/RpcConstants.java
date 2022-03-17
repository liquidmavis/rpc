package com.cuit.remoting.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 10:38
 */

public class RpcConstants {
    public static final byte[] MAGIC_NUMBER = {(byte)'m',(byte)'a',(byte)'v',(byte)'i',(byte)'s'};
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    //版本
    public static final byte VERSION = 1;
    public static final byte TOTAL_LENGTH = 17;
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    //ping和pong
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;

    public static final int HEAD_LENGTH = 17;
    public static final String PING = "ping";
    public static final String PONG = "pong";
    public static final int MAX_FRAME_LENGTH = 8*1024*1024;
}
