package com.cuit.remoting.transport.netty.client;

import com.cuit.enums.CompressTypeEnum;
import com.cuit.enums.SerializationTypeEnum;
import com.cuit.factory.SingleFactory;
import com.cuit.remoting.constants.RpcConstants;
import com.cuit.remoting.dto.RpcMessage;
import com.cuit.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 16:37
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter{
    private final UnProcessRequests unProcessRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unProcessRequests = SingleFactory.getSingle(UnProcessRequests.class);
        this.nettyRpcClient = SingleFactory.getSingle(NettyRpcClient.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            log.info("客户端收到消息{}",msg);
            if(msg instanceof RpcMessage) {
                RpcMessage tmpMsg = (RpcMessage) msg;
                byte messageType = tmpMsg.getMessageType();
                if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                    log.info("心跳[{}]",tmpMsg.getData());
                }else if (messageType == RpcConstants.RESPONSE_TYPE){
                    unProcessRequests.complete((RpcResponse<Object>) tmpMsg.getData());
                }
            }
        }finally{
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if(state == IdleState.WRITER_IDLE){
                log.info("写发生懒惰{}",ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端遇到问题",cause);
        cause.printStackTrace();
        ctx.close();
    }
}
