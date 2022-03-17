package com.cuit;

import com.cuit.annotation.RpcReference;
import com.cuit.annotation.RpcService;
import com.cuit.remoting.transport.netty.client.NettyRpcClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/10 21:23
 */


@RestController
public class HelloController extends NettyRpcClient {
    @RpcReference(group="alibaba",version = "1.0")
    private GameService gameService;

    @RequestMapping("hello")
    public void test(){
        String game = gameService.playGame("玩游戏哦");
        System.out.println(game);
    }

}
