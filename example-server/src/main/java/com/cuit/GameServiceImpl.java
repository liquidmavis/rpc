package com.cuit;

import com.cuit.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

/**
 * @PROJECT_NAME: rpc
 * @SCHOOL:CUIT
 * @USER: Liquid
 * @DATE: 2022/3/10 21:12
 */
@Slf4j
@RpcService(group="alibaba",version="1.0")
public class GameServiceImpl implements GameService{
    static {
        log.info("GameServiceImpl被创建");
    }

    @Override
    public String playGame(String message) {
        log.info("GameServiceImpl收到：{}",message);
        String info = "tencent游戏开创未来";
        log.info("GameServiceImpl返回：{}",info);
        return info;
    }
}
