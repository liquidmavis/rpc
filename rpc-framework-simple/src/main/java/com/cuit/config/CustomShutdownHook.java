package com.cuit.config;

import com.cuit.registry.util.CuratorUtils;
import com.cuit.remoting.transport.netty.server.NettyRpcServer;
import com.cuit.utils.threadpool.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/7 14:55
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook(){return CUSTOM_SHUTDOWN_HOOK;}

    public void clearAll(){
        log.info("添加钩子函数来清除");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), NettyRpcServer.PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(),inetSocketAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}
