package com.cuit.remoting.transport.netty.server;

import com.cuit.enums.CompressTypeEnum;
import com.cuit.enums.RpcResponseCodeEnum;
import com.cuit.enums.SerializationTypeEnum;
import com.cuit.factory.SingleFactory;
import com.cuit.remoting.constants.RpcConstants;
import com.cuit.remoting.dto.RpcMessage;
import com.cuit.remoting.dto.RpcRequest;
import com.cuit.remoting.dto.RpcResponse;
import com.cuit.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/7 14:51
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter{
    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingleFactory.getSingle(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof RpcMessage){
                log.info("服务器收到信息:[{}]",msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if(RpcConstants.HEARTBEAT_REQUEST_TYPE == messageType){
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                }else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    Object handler = rpcRequestHandler.handler(rpcRequest);
                    log.info("服务器收到执行结果[{}]",handler.toString());
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if(ctx.channel().isActive() && ctx.channel().isWritable()){
                        RpcResponse<Object> rpcResponse = RpcResponse.success(handler,rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    }
                    else{
                        RpcResponse<Object> fail = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(fail);
                        log.error("通道不可用，消息丢失");
                    }
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
