package com.cuit.remoting.transport.netty.Codec;

import com.cuit.compress.Compress;
import com.cuit.enums.CompressTypeEnum;
import com.cuit.enums.SerializationTypeEnum;
import com.cuit.extension.ExtensionLoader;
import com.cuit.remoting.constants.RpcConstants;
import com.cuit.remoting.dto.RpcMessage;
import com.cuit.remoting.dto.RpcRequest;
import com.cuit.remoting.dto.RpcResponse;
import com.cuit.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 16:37
 */
/**
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder{
    public RpcMessageDecoder() {
        // lengthFieldOffset: magic code is 5B, and version is 1B, and then full length. so value is 6
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 10 bytes before, so the left length is (fullLength-10). so values is -10
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(RpcConstants.MAX_FRAME_LENGTH,6,4,-10,0);
    }

    public RpcMessageDecoder(int maxFrameLength,int lengthFieldOffset,int lengthFileLength,int lengthAdjustment,int initialBytesToStrip){
        super(maxFrameLength,lengthFieldOffset,lengthFileLength,lengthAdjustment,initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if(decode instanceof ByteBuf) {
            ByteBuf decode1 = (ByteBuf) decode;
            if(decode1.readableBytes() >= RpcConstants.TOTAL_LENGTH){
                try {
                    return decodeFrame(decode1);
                } catch (Exception e) {
                    log.error("解析帧出错",e);
                    throw e;
                } finally {
                    decode1.release();
                }
            }
        }
        return decode;
    }

    private Object decodeFrame(ByteBuf in){
// note: must read ByteBuf in order
        checkMagicCode(in);
        checkVersion(in);
        int fullLength = in.readInt();
        // build RpcMessage object
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            // decompress the bytes
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(compressName);
            bs = compress.decompress(bs);
            // deserialize the object
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                    .getExtension(codecName);
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(tmpValue);
            } else {
                RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;
    }


    private void checkVersion(ByteBuf in){
        byte b = in.readByte();
        if(b != RpcConstants.VERSION){
            throw new RuntimeException("版本不对{}"+b);
        }
    }

    private void checkMagicCode(ByteBuf in){
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] bytes = new byte[len];
        in.readBytes(bytes);
        for (int i = 0; i < len ; i++) {
            if(bytes[i] != RpcConstants.MAGIC_NUMBER[i]){
                throw new RuntimeException("魔法号不匹配"+bytes.toString());
            }
        }

    }
}
