package com.cuit.remoting.transport.netty.server;

import com.cuit.config.CustomShutdownHook;
import com.cuit.config.RpcServiceConfig;
import com.cuit.factory.SingleFactory;
import com.cuit.provider.ServiceProvider;
import com.cuit.provider.impl.ZkServiceProviderImpl;
import com.cuit.remoting.transport.netty.Codec.RpcMessageDecoder;
import com.cuit.remoting.transport.netty.Codec.RpcMessageEncoder;
import com.cuit.utils.RuntimeUtil;
import com.cuit.utils.threadpool.ThreadPoolFactoryUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/7 14:51
 */
@Slf4j
@Component
public class NettyRpcServer {
    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingleFactory.getSingle(ZkServiceProviderImpl.class);

    public void registerService(RpcServiceConfig rpcServiceConfig){serviceProvider.publishService(rpcServiceConfig);}


    @SneakyThrows
    public void start(){
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String hostAddress = InetAddress.getLocalHost().getHostAddress();

        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtils.createThreadFactory("service-handler-group", false));
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                            pipeline.addLast(new RpcMessageEncoder());
                            pipeline.addLast(new RpcMessageDecoder());
                            pipeline.addLast(serviceHandlerGroup,new NettyRpcServerHandler());
                        }
                    });
            ChannelFuture sync = b.bind(hostAddress,PORT).sync();
            sync.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("启动服务器的时候发生错误{}",e);
        } finally {
            log.error("关闭所有group");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }
}
