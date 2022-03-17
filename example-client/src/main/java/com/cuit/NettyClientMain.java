package com.cuit;

import com.cuit.annotation.RpcScan;
import com.cuit.remoting.transport.netty.client.NettyRpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/10 21:25
 */
@RpcScan(basePackages = {"com.cuit"})
@SpringBootApplication
public class NettyClientMain {
    public static void main(String[] args) {
        SpringApplication.run(NettyClientMain.class);
    }
}
