package com.cuit.remoting.transport.netty.Codec;

import com.cuit.compress.Compress;
import com.cuit.enums.CompressTypeEnum;
import com.cuit.enums.SerializationTypeEnum;
import com.cuit.extension.ExtensionLoader;
import com.cuit.remoting.constants.RpcConstants;
import com.cuit.remoting.dto.RpcMessage;
import com.cuit.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 16:37
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage>{
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception {
        try {
            byteBuf.writeBytes(RpcConstants.MAGIC_NUMBER);
            byteBuf.writeByte(RpcConstants.VERSION);
            //之后再添加长度字段
            byteBuf.writerIndex(byteBuf.writerIndex()+4);
            byte messageType = rpcMessage.getMessageType();
            byteBuf.writeByte(messageType);
            byteBuf.writeByte(rpcMessage.getCodec());
            byteBuf.writeByte(CompressTypeEnum.GZIP.getCode());
            byteBuf.writeInt(ATOMIC_INTEGER.incrementAndGet());

            //建立body数据
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            //如果消息类型不是心跳，那么总行度等于头长度加body长度
            if(messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE && messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE){
                String codeName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("编码类型名字为[{}]",codeName);
                Serializer serialize = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codeName);
                bodyBytes = serialize.serialize(rpcMessage.getData());
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress =  ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }
            if(bodyBytes != null){
                byteBuf.writeBytes(bodyBytes);
            }
            int currentWriteIndex = byteBuf.writerIndex();
            byteBuf.writerIndex(currentWriteIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + RpcConstants.VERSION);
            byteBuf.writeInt(fullLength);
            byteBuf.writerIndex(currentWriteIndex);
        } catch (Exception e) {
            log.error("消息编码失败!",e);
        }
    }
}
