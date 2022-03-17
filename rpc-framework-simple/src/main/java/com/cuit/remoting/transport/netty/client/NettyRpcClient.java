package com.cuit.remoting.transport.netty.client;

import com.cuit.enums.CompressTypeEnum;
import com.cuit.enums.SerializationTypeEnum;
import com.cuit.extension.ExtensionLoader;
import com.cuit.factory.SingleFactory;
import com.cuit.registry.ServiceDiscovery;
import com.cuit.remoting.constants.RpcConstants;
import com.cuit.remoting.dto.RpcMessage;
import com.cuit.remoting.dto.RpcRequest;
import com.cuit.remoting.dto.RpcResponse;
import com.cuit.remoting.transport.RpcRequestTransport;
import com.cuit.remoting.transport.netty.Codec.RpcMessageDecoder;
import com.cuit.remoting.transport.netty.Codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.CompleteFuture;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/2/23 16:26
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private final ChannelProvider channelProvider;
    private final UnProcessRequests unProcessRequests;
    private final Bootstrap bootstrap;
    private final EventLoopGroup group;;
    private final ServiceDiscovery serviceDiscovery;

    public NettyRpcClient(){
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS))
                                .addLast(new RpcMessageEncoder())
                                .addLast(new RpcMessageDecoder())
                                .addLast(new NettyRpcClientHandler());
                    }
                });
        unProcessRequests = SingleFactory.getSingle(UnProcessRequests.class);
        channelProvider = SingleFactory.getSingle(ChannelProvider.class);
        serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
    }
    @Override
    public Object sendRPCRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        Channel channel = getChannel(inetSocketAddress);
        if(channel.isActive()){
            unProcessRequests.put(rpcRequest.getRequestId(),resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.PROTOSTUFF.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener)future->{
                if(future.isSuccess()){
                    log.info("客服端发送消息{}",rpcMessage);
                }else{
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("发送消息失败[{}]",future.cause());
                }
            });
        }else{
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> channelCompletableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener)future->{
            if(future.isSuccess()){
                log.info("客服端{}连接成功",inetSocketAddress.getHostName());
                channelCompletableFuture.complete(future.channel());
            }else{
                throw new IllegalStateException();
            }
        });
        return channelCompletableFuture.get();
    }


    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if(channel == null){
            channel= doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    public void close(){
        group.shutdownGracefully();
    }
}
