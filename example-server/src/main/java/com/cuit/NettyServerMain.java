package com.cuit;

import com.cuit.annotation.RpcScan;
import com.cuit.config.RpcServiceConfig;
import com.cuit.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/10 21:16
 */
@RpcScan(basePackages = {"com.cuit"})
public class NettyServerMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer server = context.getBean(NettyRpcServer.class);
        server.start();

    }
}
